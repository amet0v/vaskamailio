package com.nurtel.vaskamailio.view;

import com.nurtel.vaskamailio.db.config.DatabaseContextHolder;
import com.nurtel.vaskamailio.dispatcher.entity.DispatcherEntity;
import com.nurtel.vaskamailio.dispatcher.repository.DispatcherRepository;
import com.nurtel.vaskamailio.router.entity.RouterEntity;
import com.nurtel.vaskamailio.router.repository.RouterRepository;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.nurtel.vaskamailio.router.service.RouterService.*;

@Route(value = "/router", layout = MainLayout.class)
@PageTitle("Kamailio | Router")
public class RouterView extends VerticalLayout {
    private final RouterRepository routerRepository;
    private CallbackDataProvider<RouterEntity, String> dataProvider;
    private Grid<RouterEntity> routerEntityGrid;
    private String currentFilter = "";
    public static Button addButton = new Button();

    public RouterView(RouterRepository routerRepository, DispatcherRepository dispatcherRepository) {
        this.routerRepository = routerRepository;

        Boolean isAllow = MainLayout.isAllow();
        if (!isAllow) {
            Text notAllowedText = new Text("Просмотр страницы недоступен");
            add(notAllowedText);
            return;
        }

        Map<Integer, String> setidToDescription = dispatcherRepository.findAll().stream()
                .collect(Collectors.toMap(
                        DispatcherEntity::getSetid,
                        DispatcherEntity::getDescription,
                        (a, b) -> a
                ));

        routerEntityGrid = new Grid<>(RouterEntity.class, false);
        routerEntityGrid.getStyle().set("height", "80vh");

        addButton = createRouteButton(routerRepository);

        setupDbContext();
        dataProvider = DataProvider.fromFilteringCallbacks(
                query -> {
                    int offset = query.getOffset();
                    int limit = query.getLimit();
                    String filter = currentFilter == null ? "" : currentFilter;

                    return routerRepository
                            .findByDidContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                                    filter, filter,
                                    PageRequest.of(offset / limit, limit, Sort.by("id"))
                            )
                            .stream();
                },
                query -> {
                    String filter = currentFilter == null ? "" : currentFilter;
                    return routerRepository.countByDidContainingIgnoreCaseOrDescriptionContainingIgnoreCase(filter, filter);
                }
        );

        routerEntityGrid.setDataProvider(dataProvider);

        TextField filterField = getFilterField();

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        horizontalLayout.add(addButton, filterField);

        add(horizontalLayout);

        add(routerEntityGrid);

        routerEntityGrid.addColumn(RouterEntity::getId)
                .setHeader("ID")
                .setWidth("5%")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        routerEntityGrid.addColumn(RouterEntity::getDid)
                .setHeader("DID")
                .setWidth("20%")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

//        routerEntityGrid.addColumn(RouterEntity::getKeyType)
//                .setHeader("Key type")
//                .setWidth("10%")
//                .setFlexGrow(0)
//                .setSortable(true)
//                .setResizable(true);
//
//        routerEntityGrid.addColumn(RouterEntity::getValueType)
//                .setHeader("Value type")
//                .setWidth("10%")
//                .setFlexGrow(0)
//                .setSortable(true)
//                .setResizable(true);

        routerEntityGrid.addColumn(e -> {
                    String desc = setidToDescription.get(Integer.valueOf(e.getSetid()));
                    return desc == null
                            ? String.valueOf(e.getSetid())
                            : desc + " (" + e.getSetid() + ")";
                })
                .setHeader("SetID")
                .setWidth("20%")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        routerEntityGrid.addColumn(RouterEntity::getDescription)
                .setHeader("Описание")
                .setSortable(true)
                .setResizable(true);

        routerEntityGrid.addComponentColumn(routerEntity -> {
                    Button editButton = editRouteButton(routerRepository, routerEntity);
                    editButton.addThemeVariants(ButtonVariant.LUMO_WARNING);
                    editButton.getElement().getStyle()
                            .set("font-size", "20px");

                    Div wrapper = new Div(editButton);
                    wrapper.getStyle()
                            .set("display", "flex")
                            .set("justify-content", "center")
                            .set("align-items", "center")
                            .set("height", "100%");

                    return wrapper;
                })
                .setHeader("Редактировать")
                .setWidth("10%")
                .setFlexGrow(0);

        routerEntityGrid.addComponentColumn(routerEntity -> {
                    Button deleteButton = deleteRouteButton(routerRepository, routerEntity);
                    deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
                    deleteButton.getElement().getStyle()
                            .set("font-size", "20px");

                    Div wrapper = new Div(deleteButton);
                    wrapper.getStyle()
                            .set("display", "flex")
                            .set("justify-content", "center")
                            .set("align-items", "center")
                            .set("height", "100%");

                    return wrapper;
                })
                .setHeader("Удалить")
                .setWidth("10%")
                .setFlexGrow(0);

        routerEntityGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        routerEntityGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private TextField getFilterField() {
        TextField filterField = new TextField();
        filterField.setPlaceholder("Поиск...");
        filterField.setPrefixComponent(new Icon("lumo", "search"));
        filterField.setClearButtonVisible(true);
        filterField.setWidth("300px");

        filterField.addValueChangeListener(e -> {
            applyFilter(e.getValue());
        });

        return filterField;
    }

    private void applyFilter(String filterText) {
        this.currentFilter = filterText == null ? "" : filterText.trim();
        setupDbContext();
        dataProvider.refreshAll();
    }


    private Button createRouteButton(RouterRepository routerRepository) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("New entity");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        TextField didField = new TextField("DID");
        IntegerField setidField = new IntegerField("SetID");
        TextField descriptionField = new TextField("Description");

        customizeFields(didField, setidField, descriptionField);

        dialogLayout.add(didField, setidField, descriptionField);

        Button saveButton = new Button("Сохранить", e -> {
            //можно заменить "" на null и обратно
            String did = didField.isEmpty() ? null : didField.getValue();
            Integer setid = setidField.getValue();
            String description = descriptionField.isEmpty() ? null : descriptionField.getValue();
            if (did == null) {
                Notification.show("Ошибка: DID не может быть пустым", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                setupDbContext();
                createRoute(routerRepository, did, setid, description);

                Notification.show("Запись успешно создана", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (NumberFormatException exception) {
                Notification.show(exception.toString(), 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            refreshGrid();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Отмена", e -> {
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(saveButton, cancelButton);

        Button addRouteButton = new Button("Добавить", e -> {
            didField.clear();
            descriptionField.clear();
            dialog.open();
        });
        addRouteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(dialog);

        return addRouteButton;
    }

    private Button editRouteButton(RouterRepository routerRepository, RouterEntity route) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit entity");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        TextField didField = new TextField("DID");
        IntegerField setidField = new IntegerField("SetID");
        TextField descriptionField = new TextField("Description");

        customizeFields(didField, setidField, descriptionField);

        didField.setValue(route.getDid() == null ? "" : route.getDid());
        setidField.setValue(route.getSetid() == null ? 0 : Integer.parseInt(route.getSetid()));
        descriptionField.setValue(route.getDescription() == null ? "" : route.getDescription());

        dialogLayout.add(didField, setidField, descriptionField);

        Button saveButton = new Button("Сохранить", e -> {
            //можно заменить "" на null и обратно
            String did = didField.isEmpty() ? null : didField.getValue();
            Integer setid = setidField.getValue();
            String description = descriptionField.isEmpty() ? null : descriptionField.getValue();
            if (setid == null) {
                Notification.show("Ошибка: SetID не может быть пустым", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                setupDbContext();
                editRoute(routerRepository, route.getId(), did, setid, description);

                Notification.show("Запись успешно изменена", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (NumberFormatException exception) {
                Notification.show(exception.toString(), 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            refreshGrid();
            dialog.close();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Отмена", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(saveButton, cancelButton);

        Button editRouteButton = new Button("✏\uFE0F", e -> {
            dialog.open();
        });

        add(dialog);

        return editRouteButton;
    }

    private Button deleteRouteButton(RouterRepository routerRepository, RouterEntity route) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete entity");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        Text text = new Text("Подтвердите удаление маршрута");

        dialogLayout.add(text);

        Button deleteButton = new Button("Удалить", e -> {
            setupDbContext();
            deleteRoute(routerRepository, route.getId());
            refreshGrid();
            dialog.close();
            Notification.show("Запись успешно удалена", 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Отмена", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(deleteButton, cancelButton);

        Button deleteRouteButton = new Button("❌", e -> {
            dialog.open();
        });

        add(dialog);

        return deleteRouteButton;
    }

    private void customizeFields(
            TextField didField,
            IntegerField setidField,
            TextField descriptionField
    ) {
        setidField.setStepButtonsVisible(true);
        setidField.setValue(0);
        setidField.setMin(0);
    }

    private void setupDbContext() {
        getSelectedDb().ifPresent(DatabaseContextHolder::set);
    }

    private Optional<String> getSelectedDb() {
        return UI.getCurrent().getChildren()
                .filter(c -> c instanceof MainLayout)
                .map(c -> ((MainLayout) c).getDbSelector().getValue())
                .findFirst();
    }

    private void refreshGrid() {
//        List<RouterEntity> updatedItems = routerRepository.findAll(Sort.by("id"));
//        dataProvider.getItems().clear();
//        dataProvider.getItems().addAll(updatedItems);
        dataProvider.refreshAll();
    }
}
