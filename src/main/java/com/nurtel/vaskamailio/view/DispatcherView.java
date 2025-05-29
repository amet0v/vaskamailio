package com.nurtel.vaskamailio.view;

import com.nurtel.vaskamailio.dispatcher.entity.DispatcherEntity;
import com.nurtel.vaskamailio.dispatcher.repository.DispatcherRepository;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import static com.nurtel.vaskamailio.dispatcher.service.DispatcherService.deleteDispatcherEntity;

@Route(value = "/dispatcher", layout = MainLayout.class)
@PageTitle("Kamailio | Dispatcher")
public class DispatcherView extends VerticalLayout {
    public DispatcherView(DispatcherRepository dispatcherRepository) {
        add("WORK IN PROGRESS");
        Grid<DispatcherEntity> dispatcherEntityGrid = new Grid<>(DispatcherEntity.class, false);
        dispatcherEntityGrid.getStyle().set("height", "80vh");
        add(dispatcherEntityGrid);

        dispatcherEntityGrid.addColumn(DispatcherEntity::getId)
                .setHeader("ID")
                .setWidth("5%")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        dispatcherEntityGrid.addColumn(DispatcherEntity::getSetid)
                .setHeader("SetID")
//                .setWidth("5%")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        dispatcherEntityGrid.addColumn(DispatcherEntity::getDestination)
                .setHeader("Destination")
                .setSortable(true)
                .setResizable(true);

        dispatcherEntityGrid.addColumn(DispatcherEntity::getFlags)
                .setHeader("Flags")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        dispatcherEntityGrid.addColumn(DispatcherEntity::getPriority)
                .setHeader("Priority")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        dispatcherEntityGrid.addColumn(DispatcherEntity::getAttrs)
                .setHeader("Attrs")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        dispatcherEntityGrid.addColumn(DispatcherEntity::getDescription)
                .setHeader("Description")
                .setSortable(true)
                .setResizable(true);

        dispatcherEntityGrid.addComponentColumn(routerEntity -> {
                    Button editButton = new Button("✏\uFE0F");
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

        dispatcherEntityGrid.addComponentColumn(routerEntity -> {
                    Button deleteButton = new Button("❌");
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

        dispatcherEntityGrid.setItems(dispatcherRepository.findAll());

        dispatcherEntityGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        dispatcherEntityGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

//    private Button createRouteButton(DispatcherRepository dispatcherRepository, Grid<DispatcherEntity> grid) {
//        Dialog dialog = new Dialog();
//        dialog.setHeaderTitle("New entity");
//
//        VerticalLayout dialogLayout = new VerticalLayout();
//        dialog.add(dialogLayout);
//
//        TextField routeCidField = new TextField("CID example");
//        TextField routeDidField = new TextField("DID example");
//        TextField routeSetidField = new TextField("SetID example");
//        TextField routeDescriptionField = new TextField("Description example");
//
//        routeCidField.setHelperText("example cid");
//        routeDidField.setHelperText("example did");
//        routeSetidField.setHelperText("example setid");
//        routeDescriptionField.setHelperText("example desc");
//
//        dialogLayout.add(routeCidField, routeDidField, routeSetidField, routeDescriptionField);
//
//        Button saveButton = new Button("Сохранить", e -> {
//            String cid = routeCidField.isEmpty() ? null : routeCidField.getValue();
//            String did = routeDidField.isEmpty() ? null : routeDidField.getValue();
//            String setid = routeSetidField.isEmpty() ? null : routeSetidField.getValue();
//            String description = routeDescriptionField.isEmpty() ? null : routeDescriptionField.getValue();
//
//            try {
//                createRoute(routerRepository, cid, did, setid == null || setid.isEmpty() ? null : Integer.valueOf(setid), description);
//
//                Notification.show("Запись успешно создана", 5000, Notification.Position.BOTTOM_END)
//                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//            } catch (NumberFormatException exception) {
//                Notification.show("Ошибка: невозможно преобразовать SetID в число", 5000, Notification.Position.BOTTOM_END)
//                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//            }
//
//            grid.setItems(routerRepository.findAll());
//        });
//        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//        Button cancelButton = new Button("Отмена", e -> dialog.close());
//        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
//        dialog.getFooter().add(saveButton, cancelButton);
//
//        Button addRouteButton = new Button("Добавить", e -> {
//            routeCidField.clear();
//            routeDidField.clear();
//            routeSetidField.clear();
//            routeDescriptionField.clear();
//            dialog.open();
//        });
//        addRouteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//
//        add(dialog);
//
//        return addRouteButton;
//    }
//
//    private Button editRouteButton(DispatcherRepository dispatcherRepository, Grid<DispatcherEntity> grid, DispatcherEntity dispatcherEntity) {
//        Dialog dialog = new Dialog();
//        dialog.setHeaderTitle("Edit entity");
//
//        VerticalLayout dialogLayout = new VerticalLayout();
//        dialog.add(dialogLayout);
//
//        TextField routeCidField = new TextField("CID example");
//        TextField routeDidField = new TextField("DID example");
//        TextField routeSetidField = new TextField("SetID example");
//        TextField routeDescriptionField = new TextField("Description example");
//
//        routeCidField.setValue(route.getCid() == null ? "" : route.getCid());
//        routeDidField.setValue(route.getDid() == null ? "" : route.getDid());
//        routeSetidField.setValue(route.getSetid() == null ? "" : String.valueOf(route.getSetid()));
//        routeDescriptionField.setValue(route.getDescription() == null ? "" : route.getDescription());
//
//        dialogLayout.add(routeCidField, routeDidField, routeSetidField, routeDescriptionField);
//
//        Button saveButton = new Button("Сохранить", e -> {
//            String cid = routeCidField.isEmpty() ? null : routeCidField.getValue();
//            String did = routeDidField.isEmpty() ? null : routeDidField.getValue();
//            String setid = routeSetidField.isEmpty() ? null : routeSetidField.getValue();
//            String description = routeDescriptionField.isEmpty() ? null : routeDescriptionField.getValue();
//
//            try {
//                editRoute(routerRepository, route.getId(), cid, did, setid == null || setid.isEmpty() ? null : Integer.valueOf(setid), description);
//
//                Notification.show("Запись успешно создана", 5000, Notification.Position.BOTTOM_END)
//                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//            } catch (NumberFormatException exception) {
//                Notification.show("Ошибка: невозможно преобразовать SetID в число", 5000, Notification.Position.BOTTOM_END)
//                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//            }
//
//            grid.setItems(routerRepository.findAll());
//            dialog.close();
//        });
//        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
//        Button cancelButton = new Button("Отмена", e -> dialog.close());
//        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
//        dialog.getFooter().add(saveButton, cancelButton);
//
//        Button editRouteButton = new Button("✏\uFE0F", e -> {
//            dialog.open();
//        });
//
//        add(dialog);
//
//        return editRouteButton;
//    }

    private Button deleteRouteButton(DispatcherRepository dispatcherRepository, Grid<DispatcherEntity> grid, DispatcherEntity dispatcherEntity) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete entity");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        Text text = new Text("Подтвердите удаление маршрута");

        dialogLayout.add(text);

        Button deleteButton = new Button("Удалить", e -> {
            deleteDispatcherEntity(dispatcherRepository, dispatcherEntity.getId());

            grid.setItems(dispatcherRepository.findAll());
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
}
