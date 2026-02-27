package com.nurtel.vaskamailio.view;

import com.nurtel.vaskamailio.cdr.entity.CdrEntity;
import com.nurtel.vaskamailio.cdr.repository.CdrRepository;
import com.nurtel.vaskamailio.db.config.DatabaseContextHolder;
import com.nurtel.vaskamailio.dispatcher.entity.DispatcherEntity;
import com.nurtel.vaskamailio.dispatcher.repository.DispatcherRepository;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "/cdr", layout = MainLayout.class)
@PageTitle("Kamailio | CDR")
public class CdrView extends VerticalLayout {
    Map<Integer, String> setidToDescription;
    Map<String, String> sourceToDescription;

    public CdrView(CdrRepository cdrRepository, DispatcherRepository dispatcherRepository) {
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
        });
        selectCdrButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        selectCdrButton.getStyle().set("margin-top", "35px");
        selectCdrButton.addClickShortcut(Key.ENTER);
        selectCdrButton.addClickShortcut(Key.NUMPAD_ENTER);

        filterLayout.add(sourceField, cidField, didField, setidField, selectCdrButton);

        add(filterLayout);

//        List<CdrEntity> cdrList = cdrRepository.findAll();
//        cdrGrid.setItems(cdrList);

        cdrGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        cdrGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        add(cdrGrid);
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
