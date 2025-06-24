package com.nurtel.vaskamailio.view;

import com.nurtel.vaskamailio.router.entity.RouterEntity;
import com.nurtel.vaskamailio.router.repository.RouterRepository;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.nurtel.vaskamailio.router.service.RouterService.*;

@Route(value = "/router", layout = MainLayout.class)
@PageTitle("Kamailio | Router")
public class RouterView extends VerticalLayout {
    ListDataProvider<RouterEntity> dataProvider;
    public static Button addButton = new Button();
//    public static Button deleteButton = new Button();
//    public static Button editButton = new Button();

    public RouterView(RouterRepository routerRepository) {
        Grid<RouterEntity> routerEntityGrid = new Grid<>(RouterEntity.class, false);
        routerEntityGrid.getStyle().set("height", "80vh");

        addButton = createRouteButton(routerRepository, routerEntityGrid);

        List<RouterEntity> items = routerRepository.findAll(Sort.by("id"));
        dataProvider = new ListDataProvider<>(items);
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
                .setHeader("DID (key)")
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

        routerEntityGrid.addColumn(RouterEntity::getSetid)
                .setHeader("SetID (value)")
                .setSortable(true)
                .setResizable(true);

        routerEntityGrid.addColumn(RouterEntity::getDescription)
                .setHeader("Описание")
                .setSortable(true)
                .setResizable(true);

        routerEntityGrid.addComponentColumn(routerEntity -> {
                    Button editButton = editRouteButton(routerRepository, routerEntityGrid, routerEntity);
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
                    Button deleteButton = deleteRouteButton(routerRepository, routerEntityGrid, routerEntity);
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

//        routerEntityGrid.setItems(routerRepository.findAll(Sort.by("id")));

        routerEntityGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        routerEntityGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private TextField getFilterField() {
        TextField filterField = new TextField();
        filterField.setPlaceholder("Поиск...");
        filterField.setClearButtonVisible(true);
        filterField.setWidth("300px");
        filterField.addValueChangeListener(e ->
                dataProvider.setFilter(router -> {
                    String value = e.getValue().toLowerCase();
                    return (router.getDid() != null && router.getDid().toLowerCase().contains(value))
                            || (router.getDescription() != null && router.getDescription().toLowerCase().contains(value));
                }));
        return filterField;
    }

    private Button createRouteButton(RouterRepository routerRepository, Grid<RouterEntity> grid) {
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
            if (setid == null) {
                Notification.show("Ошибка: SetID не может быть пустым", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                createRoute(routerRepository, did, setid, description);

                Notification.show("Запись успешно создана", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (NumberFormatException exception) {
                Notification.show("Ошибка: невозможно преобразовать SetID в число", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            refreshGrid(routerRepository, dataProvider);
//            grid.setItems(routerRepository.findAll(Sort.by("id")));
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Отмена", e -> {
            setidField.clear();
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(saveButton, cancelButton);

        Button addRouteButton = new Button("Добавить", e -> {
//            cidField.clear();
            didField.clear();
            descriptionField.clear();
            dialog.open();
        });
        addRouteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(dialog);

        return addRouteButton;
    }

    private Button editRouteButton(RouterRepository routerRepository, Grid<RouterEntity> grid, RouterEntity route) {
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
                editRoute(routerRepository, route.getId(), did, setid, description);

                Notification.show("Запись успешно создана", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (NumberFormatException exception) {
                Notification.show("Ошибка: невозможно преобразовать SetID в число", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            refreshGrid(routerRepository, dataProvider);
//            grid.setItems(routerRepository.findAll(Sort.by("id")));
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

    private Button deleteRouteButton(RouterRepository routerRepository, Grid<RouterEntity> grid, RouterEntity route) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete entity");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        Text text = new Text("Подтвердите удаление маршрута");

        dialogLayout.add(text);

        Button deleteButton = new Button("Удалить", e -> {
            deleteRoute(routerRepository, route.getId());
            refreshGrid(routerRepository, dataProvider);
//            grid.setItems(routerRepository.findAll(Sort.by("id")));
            dialog.close();
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

//        didField.setHelperText("example: 17888");
//        setidField.setHelperText("example setid");
//        descriptionField.setHelperText("example desc");
    }

    private void refreshGrid(RouterRepository routerRepository, ListDataProvider<RouterEntity> dataProvider) {
        List<RouterEntity> updatedItems = routerRepository.findAll(Sort.by("id"));
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(updatedItems);
        dataProvider.refreshAll();
    }
}
