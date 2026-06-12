package com.nurtel.vaskamailio.view;

import com.nurtel.vaskamailio.cdr.entity.CdrEntity;
import com.nurtel.vaskamailio.cdr.repository.CdrRepository;
import com.nurtel.vaskamailio.db.config.DatabaseContextHolder;
import com.nurtel.vaskamailio.dispatcher.entity.DispatcherEntity;
import com.nurtel.vaskamailio.dispatcher.repository.DispatcherRepository;
import com.nurtel.vaskamailio.prefix.entity.PrefixEntity;
import com.nurtel.vaskamailio.prefix.repository.PrefixRepository;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.icepear.echarts.Line;
import org.icepear.echarts.charts.line.LineSeries;
import org.icepear.echarts.components.coord.cartesian.CategoryAxis;
import org.icepear.echarts.render.Engine;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "/cdr", layout = MainLayout.class)
@PageTitle("Kamailio | CDR")
public class CdrView extends VerticalLayout {
    private final PrefixRepository prefixRepository;
    Map<Integer, String> setidToDescription;
    Map<String, String> sourceToDescription;

    public CdrView(CdrRepository cdrRepository, DispatcherRepository dispatcherRepository, PrefixRepository prefixRepository) {
        this.prefixRepository = prefixRepository;
        Boolean isAllow = MainLayout.isAllow();
        if (!isAllow) {
            Text notAllowedText = new Text("Просмотр страницы недоступен");
            add(notAllowedText);
            return;
        }

        Grid<CdrEntity> cdrGrid = new Grid<>(CdrEntity.class, false);
        cdrGrid.setHeight("75vh");

//        cdrGrid.addColumn(CdrEntity::getId)
//                .setHeader("ID")
//                .setSortable(true)
//                .setResizable(true);

        cdrGrid.addColumn(CdrEntity::getCallTime)
                .setHeader("Call Time")
                .setSortable(true)
                .setResizable(true);

        cdrGrid.addColumn(cdr -> {
                    String desc = sourceToDescription.get("sip:" + cdr.getSource());

                    if (desc != null) {
                        desc = desc.replaceAll("[_\\d]", "");
                    }

                    return desc == null
                            ? String.valueOf(cdr.getSource())
                            : desc + " (" + cdr.getSource() + ")";
                })
                .setHeader("Source")
                .setSortable(true)
                .setResizable(true);

        cdrGrid.addColumn(CdrEntity::getCid)
                .setHeader("CID")
                .setSortable(true)
                .setResizable(true);

        cdrGrid.addColumn(CdrEntity::getDid)
                .setHeader("DID")
                .setSortable(true)
                .setResizable(true);

        cdrGrid.addColumn(cdr -> {
                    String desc = setidToDescription.get(cdr.getSetid());

                    if (desc != null) {
                        desc = desc.replaceAll("[_\\d]", "");
                    }

                    return desc == null
                            ? String.valueOf(cdr.getSetid())
                            : desc + " (setid: " + cdr.getSetid() + ")";
                })
                .setHeader("SetID")
                .setSortable(true)
                .setResizable(true);

        cdrGrid.addColumn(CdrEntity::getReason)
                .setHeader("Reason")
                .setSortable(true)
                .setResizable(true);

        cdrGrid.setItems();

        HorizontalLayout filterLayout = new HorizontalLayout();

        DateTimePicker startDateTimePicker = new DateTimePicker("От");
        startDateTimePicker.setValue(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0));
        startDateTimePicker.setWidth("250px");

        DateTimePicker endDateTimePicker = new DateTimePicker("До");
        endDateTimePicker.setValue(LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999));
        endDateTimePicker.setWidth("250px");

        startDateTimePicker.setLocale(Locale.of("ru", "RU"));
        endDateTimePicker.setLocale(Locale.of("ru", "RU"));

        filterLayout.add(startDateTimePicker, endDateTimePicker);

        TextField sourceField = new TextField("Source");
        TextField cidField = new TextField("CID");
        TextField didField = new TextField("DID");
        IntegerField setidField = new IntegerField("SetID");
        List<CdrEntity> chartItems = new ArrayList<>();

        Button selectCdrButton = new Button("Поиск", e -> {
            if (startDateTimePicker.isEmpty() && endDateTimePicker.isEmpty()) {
                Notification.show("Укажите временной промежуток для поиска CDR ", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            setupDbContext(dispatcherRepository);

            List<CdrEntity> items = cdrRepository.findByFieldsInTimeRange(
                    startDateTimePicker.getValue(),
                    endDateTimePicker.getValue(),
                    sourceField.isEmpty() ? "" : sourceField.getValue(),
                    cidField.isEmpty() ? "" : cidField.getValue(),
                    didField.isEmpty() ? "" : didField.getValue(),
                    setidField.isEmpty() ? null : setidField.getValue()
            );
            System.out.println(startDateTimePicker.getValue());
            System.out.println(endDateTimePicker.getValue());
            cdrGrid.setItems(items);
            chartItems.clear();
            chartItems.addAll(items);
        });

        Button buildChartButton = new Button("График", e -> {
            if (chartItems.isEmpty()) {
                Notification.show("Сделайте выборку по CDR", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
                Dialog dialog = new Dialog();

                createRoutingChart(dialog, chartItems, prefixRepository);

                dialog.open();
            }
        });

        selectCdrButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        selectCdrButton.getStyle().set("margin-top", "35px");
        selectCdrButton.addClickShortcut(Key.ENTER);
        selectCdrButton.addClickShortcut(Key.NUMPAD_ENTER);

        buildChartButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buildChartButton.getStyle().set("margin-top", "35px");

        filterLayout.add(sourceField, cidField, didField, setidField, selectCdrButton, buildChartButton);

        add(filterLayout);

//        List<CdrEntity> cdrList = cdrRepository.findAll();
//        cdrGrid.setItems(cdrList);

        cdrGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        cdrGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        add(cdrGrid);
    }

    private void createRoutingChart(Dialog dialog, List<CdrEntity> chartItems, PrefixRepository prefixRepository) {
        dialog.removeAll();
        dialog.getHeader().removeAll();

        List<Line> lines = new ArrayList<>();

        Map<String, Map<Integer, Long>> data =
                chartItems.stream()
                        .collect(Collectors.groupingBy(
                                c -> c.getCallTime().withMinute(0)
                                        .withSecond(0)
                                        .withNano(0)
                                        .toString(),
                                TreeMap::new,
                                Collectors.groupingBy(
                                        CdrEntity::getSetid,
                                        Collectors.counting()
                                )
                        ));

        Set<Integer> allSetids = chartItems.stream()
                .map(CdrEntity::getSetid)
                .collect(Collectors.toSet());

        List<LineSeries> seriesList = new ArrayList<>();

        for (Integer setid : allSetids) {
            List<Long> values = new ArrayList<>();

            for (String hour : data.keySet()) {
                Long count = data.getOrDefault(hour, Map.of())
                        .getOrDefault(setid, 0L);

                values.add(count);
            }

            seriesList.add(
                    new LineSeries()
                            .setName(setidToDescription.get(setid).replaceAll("[_\\d]", ""))
                            .setData(values.toArray(new Number[0]))
            );
        }

        Line line = new Line()
                .setTitle("Traffic by setID")
                .setTooltip("axis")
                .setLegend()
                .addXAxis(
                        new CategoryAxis()
                                .setData(data.keySet().toArray(new String[0]))
                )
                .addYAxis();

        for (LineSeries s : seriesList) {
            line.addSeries(s);
        }

        lines.add(line);

        //for each setid
        Set<Integer> setidSet = chartItems.stream()
                .map(CdrEntity::getSetid)
                .collect(Collectors.toSet());

        for (Integer setid : setidSet) {
            Map<String, Map<String, Long>> setidData =
                    chartItems.stream()
                            .filter(c -> Objects.equals(c.getSetid(), setid))
                            .collect(Collectors.groupingBy(
                                    c -> c.getCallTime().withMinute(0)
                                            .withSecond(0)
                                            .withNano(0)
                                            .toString(),
                                    TreeMap::new,
                                    Collectors.groupingBy(
                                            CdrEntity::getReason,
                                            Collectors.counting()
                                    )
                            ));

            List<LineSeries> setidSeriesList = new ArrayList<>();

            Set<String> setidReasons = setidData.values().stream()
                    .flatMap(map -> map.keySet().stream())
                    .collect(Collectors.toSet());

            for (String reason : setidReasons) {

                List<Long> values = new ArrayList<>();

                for (String hour : setidData.keySet()) {

                    Long count = setidData.getOrDefault(hour, Map.of())
                            .getOrDefault(reason, 0L);

                    values.add(count);
                }

                setidSeriesList.add(
                        new LineSeries()
                                .setName(reason)
                                .setData(values.toArray(new Number[0]))
                );
            }

            Line setidLine = new Line()
                    .setTitle(setidToDescription.get(setid).replaceAll("[_\\d]", "") + "(setid:" + setid + ")")
                    .setTooltip("axis")
                    .setLegend()
                    .addXAxis(
                            new CategoryAxis()
                                    .setData(setidData.keySet().toArray(new String[0]))
                    )
                    .addYAxis();

            for (LineSeries s : setidSeriesList) {
                setidLine.addSeries(s);
            }
            lines.add(setidLine);
        }
        Engine engine = new Engine();
        dialog.setWidth("100%");
        dialog.setHeight("100%");

        for (Line l : lines) {
            String renderHtml = engine.renderHtml(l);

            String base64 = Base64.getEncoder()
                    .encodeToString(renderHtml.getBytes(StandardCharsets.UTF_8));

            IFrame frame = new IFrame(
                    "data:text/html;base64," + base64
            );

            frame.setWidth("100%");
            frame.setHeight("100%");

            dialog.add(frame);
        }

        Button prefixChartButton = new Button("Префиксы", event -> {
            createPrefixChart(dialog, chartItems, prefixRepository);
        });

        Button closeButton = new Button(new Icon("lumo", "cross"),
                (event) -> {
                    dialog.close();
                    dialog.removeAll();
                });

        dialog.setHeaderTitle("Графики " + ComponentUtil.getData(UI.getCurrent(), "selectedDb").toString());
        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getHeader().add(prefixChartButton, closeButton);
    }

    private void createPrefixChart(Dialog dialog, List<CdrEntity> chartItems, PrefixRepository prefixRepository) {
        dialog.removeAll();
        dialog.getHeader().removeAll();

        Object value = ComponentUtil.getData(UI.getCurrent(), "selectedDb");
        String db = value != null ? value.toString() : null;
        if (db != null) {
            DatabaseContextHolder.set(db);
        }

        List<Line> lines = new ArrayList<>();
        List<String> chartPrefixes = new ArrayList<>();

        List<String> prefixes = prefixRepository.findAll()
                .stream()
                .map(PrefixEntity::getPattern)
                .toList();

        Map<String, Map<String, Long>> data = chartItems.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCallTime()
                                .withMinute(0)
                                .withSecond(0)
                                .withNano(0)
                                .toString(),
                        TreeMap::new,
                        Collectors.groupingBy(
                                c -> prefixes.stream()
                                        .filter(p -> c.getDid().matches(p))
                                        .findFirst()
                                        .orElse("UNKNOWN"),
                                Collectors.counting()
                        )
                ));

        List<LineSeries> seriesList = new ArrayList<>();

        for (String prefix : prefixes) {
            List<Long> values = new ArrayList<>();

            for (String hour : data.keySet()) {
                Long count = data.getOrDefault(hour, Map.of())
                        .getOrDefault(prefix, 0L);

                values.add(count);

                if (count > 0) {
                    if (!chartPrefixes.contains(prefix)) chartPrefixes.add(prefix);
                }
            }
            if (chartPrefixes.contains(prefix)) {
                seriesList.add(
                        new LineSeries()
                                .setName(prefix)
                                .setData(values.toArray(new Number[0]))
                );
            }
        }

        Line line = new Line()
                .setTitle("Traffic by prefixes")
                .setTooltip("axis")
                .setLegend()
                .addXAxis(
                        new CategoryAxis()
                                .setData(data.keySet().toArray(new String[0]))
                )
                .addYAxis();

        for (LineSeries s : seriesList) {
            line.addSeries(s);
        }

        lines.add(line);

        //for each prefix
        for (String prefix : chartPrefixes){
            Map<String, Map<String, Long>> prefixData = chartItems.stream()
                    .filter(c -> c.getDid().matches(prefix))
                    .collect(Collectors.groupingBy(
                            c -> c.getCallTime()
                                    .withMinute(0)
                                    .withSecond(0)
                                    .withNano(0)
                                    .toString(),
                            TreeMap::new,
                            Collectors.groupingBy(
                                    CdrEntity::getReason,
                                    Collectors.counting()
                            )
                    ));

            List<LineSeries> prefixSeriesList = new ArrayList<>();

            Set<String> prefixReasons = prefixData.values().stream()
                    .flatMap(map -> map.keySet().stream())
                    .collect(Collectors.toSet());

            for (String reason : prefixReasons) {

                List<Long> values = new ArrayList<>();

                for (String hour : prefixData.keySet()) {

                    Long count = prefixData.getOrDefault(hour, Map.of())
                            .getOrDefault(reason, 0L);

                    values.add(count);
                }

                prefixSeriesList.add(
                        new LineSeries()
                                .setName(reason)
                                .setData(values.toArray(new Number[0]))
                );
            }

            Line prefixLine = new Line()
                    .setTitle(prefix)
                    .setTooltip("axis")
                    .setLegend()
                    .addXAxis(
                            new CategoryAxis()
                                    .setData(prefixData.keySet().toArray(new String[0]))
                    )
                    .addYAxis();

            for (LineSeries s : prefixSeriesList) {
                prefixLine.addSeries(s);
            }
            lines.add(prefixLine);
        }
        Engine engine = new Engine();
        dialog.setWidth("100%");

        for (Line l : lines) {
            String renderHtml = engine.renderHtml(l);

            String base64 = Base64.getEncoder()
                    .encodeToString(renderHtml.getBytes(StandardCharsets.UTF_8));

            IFrame frame = new IFrame(
                    "data:text/html;base64," + base64
            );

            frame.setWidth("100%");
            frame.setHeight("100%");

            dialog.add(frame);
        }

        Button routingxChartButton = new Button("Маршруты", event -> {
            createRoutingChart(dialog, chartItems, prefixRepository);
        });

        Button closeButton = new Button(new Icon("lumo", "cross"),
                (event) -> {
                    dialog.close();
                    dialog.removeAll();
                });

        dialog.setHeaderTitle("Графики " + ComponentUtil.getData(UI.getCurrent(), "selectedDb").toString());
        closeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getHeader().add(routingxChartButton, closeButton);
    }

    private void setupDbContext(DispatcherRepository dispatcherRepository) {
        Object value = ComponentUtil.getData(UI.getCurrent(), "selectedDb");
        String db = value != null ? value.toString() : null;
        if (db != null) {
            DatabaseContextHolder.set(db);
        }

        setidToDescription = dispatcherRepository.findAll().stream()
                .collect(Collectors.toMap(
                        DispatcherEntity::getSetid,
                        DispatcherEntity::getDescription,
                        (a, b) -> a
                ));

        sourceToDescription = dispatcherRepository.findAll().stream()
                .collect(Collectors.toMap(
                        DispatcherEntity::getDestination,
                        DispatcherEntity::getDescription,
                        (a, b) -> a
                ));
    }
}
