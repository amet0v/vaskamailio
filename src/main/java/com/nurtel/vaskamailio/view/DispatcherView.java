package com.nurtel.vaskamailio.view;

import com.nurtel.vaskamailio.dispatcher.entity.DispatcherEntity;
import com.nurtel.vaskamailio.dispatcher.repository.DispatcherRepository;
import com.nurtel.vaskamailio.host.entity.HostEntity;
import com.nurtel.vaskamailio.host.repository.HostRepository;
import com.vaadin.flow.component.Text;
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
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.nurtel.vaskamailio.dispatcher.service.DispatcherService.*;
import static com.nurtel.vaskamailio.host.service.HostService.*;

@Route(value = "/dispatcher", layout = MainLayout.class)
@PageTitle("Kamailio | Dispatcher")
public class DispatcherView extends VerticalLayout {
    ListDataProvider<DispatcherEntity> dataProvider;
    public static Button addButton = new Button();

    public DispatcherView(DispatcherRepository dispatcherRepository, HostRepository hostRepository) {
        Grid<DispatcherEntity> dispatcherEntityGrid = new Grid<>(DispatcherEntity.class, false);
        dispatcherEntityGrid.getStyle().set("height", "80vh");

        addButton = createDispatcherButton(dispatcherRepository, hostRepository);

        List<DispatcherEntity> items = dispatcherRepository.findAll(Sort.by("id"));
        dataProvider = new ListDataProvider<>(items);
        dispatcherEntityGrid.setDataProvider(dataProvider);

        Div info = new Div(new Text("ℹ\uFE0F setid = 0 — применяется если не найден маршрут в router ℹ\uFE0F"));
        info.getElement().getThemeList().add("badge");
        info.getStyle().set("font-size", "16px");

        TextField filterField = getFilterField();

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        horizontalLayout.add(addButton, info,  filterField);
        add(horizontalLayout);

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
                //.setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        dispatcherEntityGrid.addColumn(DispatcherEntity::getDescription)
                .setHeader("Description")
                .setSortable(true)
                .setResizable(true);

        dispatcherEntityGrid.addComponentColumn(entity -> {
                    Button editButton = editDispatcherButton(dispatcherRepository, entity, hostRepository);
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

        dispatcherEntityGrid.addComponentColumn(entity -> {
                    Button deleteButton = deleteDispatcherButton(dispatcherRepository, entity, hostRepository);
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

//        dispatcherEntityGrid.setItems(items);

        dispatcherEntityGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        dispatcherEntityGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private TextField getFilterField() {
        TextField filterField = new TextField();
        filterField.setPlaceholder("Поиск...");
        filterField.setPrefixComponent(new Icon("lumo", "search"));
        filterField.setClearButtonVisible(true);
        filterField.setWidth("300px");
        filterField.addValueChangeListener(e ->
                dataProvider.setFilter(dispatcher -> {
                    String value = e.getValue().toLowerCase();
                    return (dispatcher.getDestination() != null && dispatcher.getDestination().toLowerCase().contains(value))
                            || (dispatcher.getDescription() != null && dispatcher.getDescription().toLowerCase().contains(value));
                }));
        return filterField;
    }

    private Button createDispatcherButton(DispatcherRepository dispatcherRepository, HostRepository hostRepository) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("New entity");
        dialog.setDraggable(true);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        IntegerField setidField = new IntegerField("SetID");
        TextField destinationField = new TextField("Destination");
        IntegerField flagsField = new IntegerField("Flags");
        IntegerField priorityField = new IntegerField("Priority");
        TextField attrsField = new TextField("Attrs");
        TextField descriptionField = new TextField("Description");

        customizeFields(setidField, destinationField, flagsField, priorityField, attrsField, descriptionField);

        dialogLayout.add(setidField, destinationField, flagsField, priorityField, attrsField, descriptionField);

        Button saveButton = new Button("Сохранить", e -> {

            Integer setid = setidField.getValue();
            String destination = destinationField.isEmpty() ? null : destinationField.getValue();
            Integer flags = flagsField.getValue();
            Integer priority = priorityField.getValue();
            String attrs = attrsField.isEmpty() ? "" : attrsField.getValue();
            String description = descriptionField.isEmpty() ? "" : descriptionField.getValue();

            if (setid == null || flags == null || priority == null || destination == null) {
                Notification.show("Ошибка: Destination не может быть пустым", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                createDispatcherEntity(
                        dispatcherRepository,
                        setid,
                        destination,
                        flags,
                        priority,
                        attrs,
                        description
                );

                createHost(
                        hostRepository,
                        getIpFromDestination(destination),
                        1,
                        description
                );
                Notification.show("Запись успешно создана", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (NumberFormatException | NullPointerException exception) {
                Notification.show(exception.toString(), 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            refreshGrid(dispatcherRepository, dataProvider);
//            grid.setItems(dispatcherRepository.findAll(Sort.by("id")));
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Отмена", e ->
        {
            setidField.clear();
            priorityField.clear();
            flagsField.clear();
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(saveButton, cancelButton);

        Button addRouteButton = new Button("Добавить", e -> {
            destinationField.clear();
//            attrsField.clear();
            descriptionField.clear();
            dialog.open();
        });
        addRouteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(dialog);

        return addRouteButton;
    }

    private Button editDispatcherButton(DispatcherRepository dispatcherRepository, DispatcherEntity dispatcherEntity, HostRepository hostRepository) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit entity");
        dialog.setDraggable(true);

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        IntegerField setidField = new IntegerField("SetID");
        TextField destinationField = new TextField("Destination");
        IntegerField flagsField = new IntegerField("Flags");
        IntegerField priorityField = new IntegerField("Priority");
        TextField attrsField = new TextField("Attrs");
        TextField descriptionField = new TextField("Description");

        customizeFields(setidField, destinationField, flagsField, priorityField, attrsField, descriptionField);

        setidField.setValue(dispatcherEntity.getSetid() == null ? 0 : dispatcherEntity.getSetid());
        destinationField.setValue(dispatcherEntity.getDestination() == null ? "" : dispatcherEntity.getDestination());
        flagsField.setValue(dispatcherEntity.getFlags() == null ? 0 : dispatcherEntity.getFlags());
        priorityField.setValue(dispatcherEntity.getPriority() == null ? 0 : dispatcherEntity.getPriority());
        attrsField.setValue(dispatcherEntity.getAttrs() == null ? "" : dispatcherEntity.getAttrs());
        descriptionField.setValue(dispatcherEntity.getDescription() == null ? "" : dispatcherEntity.getDescription());

        dialogLayout.add(setidField, destinationField, flagsField, priorityField, attrsField, descriptionField);

        Button saveButton = new Button("Сохранить", e -> {
            Integer setid = setidField.getValue();
            String destination = destinationField.isEmpty() ? null : destinationField.getValue();
            Integer flags = flagsField.getValue();
            Integer priority = priorityField.getValue();
            String attrs = attrsField.isEmpty() ? "" : attrsField.getValue();
            String description = descriptionField.isEmpty() ? "" : descriptionField.getValue();

            if (setid == null || flags == null || priority == null || destination == null) {
                Notification.show("Ошибка: Destination не может быть пустым", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                HostEntity host = getHostByIp(hostRepository, getIpFromDestination(dispatcherEntity.getDestination()));
                editHost(
                        hostRepository,
                        host.getId(),
                        getIpFromDestination(destination),
                        1,
                        description
                );

                editDispatcherEntity(
                        dispatcherRepository,
                        dispatcherEntity.getId(),
                        setid,
                        destination,
                        flags,
                        priority,
                        attrs,
                        description
                );

                Notification.show("Запись успешно изменена", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (NumberFormatException | NullPointerException exception) {
                Notification.show(exception.toString(), 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            refreshGrid(dispatcherRepository, dataProvider);
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

    private Button deleteDispatcherButton(DispatcherRepository dispatcherRepository, DispatcherEntity dispatcherEntity, HostRepository hostRepository) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete entity");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        Text text = new Text("Подтвердите удаление маршрута");

        dialogLayout.add(text);

        Button deleteButton = new Button("Удалить", e -> {
            try {
                HostEntity host = getHostByIp(hostRepository, getIpFromDestination(dispatcherEntity.getDestination()));
                deleteHost(hostRepository, host.getId());
            } catch (NullPointerException exception) {
                Notification.show(exception.toString(), 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            deleteDispatcherEntity(dispatcherRepository, dispatcherEntity.getId());

            refreshGrid(dispatcherRepository, dataProvider);

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
            IntegerField setidField,
            TextField destinationField,
            IntegerField flagsField,
            IntegerField priorityField,
            TextField attrsField,
            TextField descriptionField
    ) {

        setidField.setStepButtonsVisible(true);
        setidField.setValue(0);
        setidField.setMin(0);
        setidField.setWidth("255px");

        destinationField.setWidth("255px");

        flagsField.setStepButtonsVisible(true);
        flagsField.setValue(0);
        flagsField.setMin(0);
        flagsField.setWidth("255px");

        priorityField.setStepButtonsVisible(true);
        priorityField.setValue(0);
        priorityField.setMin(0);
        priorityField.setWidth("255px");

        attrsField.setValue("socket=udp:172.27.201.166:5060");
        attrsField.setWidth("255px");

        descriptionField.setWidth("255px");

//        setidField.setHelperText("0-999");
        destinationField.setHelperText("sip:172.27.x.x:5060");
        flagsField.setHelperText("0 - default");
//        priorityField.setHelperText("example priority");
        attrsField.setHelperText("socket=udp:172.27.201.166:5060");
        descriptionField.setHelperText("hostname");
    }

    private void refreshGrid(DispatcherRepository dispatcherRepository, ListDataProvider<DispatcherEntity> dataProvider) {
        List<DispatcherEntity> updatedItems = dispatcherRepository.findAll(Sort.by("id"));
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(updatedItems);
        dataProvider.refreshAll();
    }
}
