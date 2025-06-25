package com.nurtel.vaskamailio.view;

import com.nurtel.vaskamailio.host.repository.HostRepository;
import com.nurtel.vaskamailio.host.entity.HostEntity;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;

import static com.nurtel.vaskamailio.host.service.HostService.*;

@Route(value = "/hosts", layout = MainLayout.class)
@PageTitle("Kamailio | Hosts")
public class HostView extends VerticalLayout {
    ListDataProvider<HostEntity> dataProvider;
    public static Button addButton = new Button();

    public HostView(HostRepository hostRepository) {
        Grid<HostEntity> HostEntityGrid = new Grid<>(HostEntity.class, false);
        HostEntityGrid.getStyle().set("height", "80vh");

        addButton = createHostButton(hostRepository);

        List<HostEntity> items = hostRepository.findAll(Sort.by("id"));
        dataProvider = new ListDataProvider<>(items);
        HostEntityGrid.setDataProvider(dataProvider);

        Div alert = new Div(new Text("⚠️ Данные синхронизированы с dispatcher. Изменения вручную — только в исключительных случаях! ⚠️"));
        alert.getElement().getThemeList().add("badge error");

        TextField filterField = getFilterField();

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        horizontalLayout.add(addButton, alert,  filterField);
        add(horizontalLayout);

        add(HostEntityGrid);

        HostEntityGrid.addColumn(HostEntity::getId)
                .setHeader("ID")
                .setWidth("5%")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        HostEntityGrid.addColumn(HostEntity::getIp)
                .setHeader("IP")
                .setSortable(true)
                .setResizable(true);

//        HostEntityGrid.addColumn(HostEntity::getKeyType)
//                .setHeader("Key type")
//                .setWidth("10%")
//                .setFlexGrow(0)
//                .setSortable(true)
//                .setResizable(true);
//
//        HostEntityGrid.addColumn(HostEntity::getValueType)
//                .setHeader("Value type")
//                .setWidth("10%")
//                .setFlexGrow(0)
//                .setSortable(true)
//                .setResizable(true);

        HostEntityGrid.addColumn(HostEntity::getDescription)
                .setHeader("Описание")
                .setSortable(true)
                .setResizable(true);

        HostEntityGrid.addComponentColumn(entity -> {
                    Checkbox checkbox = new Checkbox();
                    checkbox.setValue(Objects.equals(entity.getIsActive(), "1"));
                    checkbox.setReadOnly(true); // Чтобы пользователь не мог менять значение
                    return checkbox;
                }).setHeader("Активно?")
                .setSortable(true)
                .setResizable(true);

        HostEntityGrid.addComponentColumn(HostEntity -> {
                    Button editButton = editHostButton(hostRepository, HostEntity);
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

        HostEntityGrid.addComponentColumn(HostEntity -> {
                    Button deleteButton = deleteHostButton(hostRepository, HostEntity);
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

//        HostEntityGrid.setItems(hostRepository.findAll(Sort.by("id")));

        HostEntityGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        HostEntityGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private TextField getFilterField() {
        TextField filterField = new TextField();
        filterField.setPlaceholder("Поиск...");
        filterField.setPrefixComponent(new Icon("lumo", "search"));
        filterField.setClearButtonVisible(true);
        filterField.setWidth("300px");
        filterField.addValueChangeListener(e ->
                dataProvider.setFilter(router -> {
                    String value = e.getValue().toLowerCase();
                    return (router.getIp() != null && router.getIp().toLowerCase().contains(value))
                            || (router.getDescription() != null && router.getDescription().toLowerCase().contains(value));
                }));
        return filterField;
    }

    private Button createHostButton(HostRepository hostRepository) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("New entity");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        TextField ipField = new TextField("IP");
        TextField descriptionField = new TextField("Description");
        Checkbox isActiveCheckbox = new Checkbox("Active");
        isActiveCheckbox.setValue(true);

        customizeFields(ipField, descriptionField);

        dialogLayout.add(ipField, descriptionField, isActiveCheckbox);

        Button saveButton = new Button("Сохранить", e -> {
            //можно заменить "" на null и обратно
            String ip = ipField.isEmpty() ? null : ipField.getValue();
            Integer isActive = isActiveCheckbox.getValue() ? 1 : 0;
            String description = descriptionField.isEmpty() ? null : descriptionField.getValue();
            if (ip == null) {
                Notification.show("Ошибка: IP не может быть пустым", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                createHost(hostRepository, ip, isActive, description);

                Notification.show("Запись успешно создана", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (NumberFormatException exception) {
                Notification.show(exception.toString(), 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            refreshGrid(hostRepository, dataProvider);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Отмена", e -> {
            ipField.clear();
            descriptionField.clear();
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(saveButton, cancelButton);

        Button addRouteButton = new Button("Добавить", e -> {
//            cidField.clear();
            ipField.clear();
            descriptionField.clear();
            dialog.open();
        });
        addRouteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(dialog);

        return addRouteButton;
    }

    private Button editHostButton(HostRepository hostRepository, HostEntity host) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit entity");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        TextField ipField = new TextField("IP");
        TextField descriptionField = new TextField("Description");
        Checkbox isActiveCheckbox = new Checkbox("Active");
        isActiveCheckbox.setValue(Objects.equals(host.getIsActive(), "1"));

        customizeFields(ipField, descriptionField);

        ipField.setValue(host.getIp() == null ? "" : host.getIp());
        descriptionField.setValue(host.getDescription() == null ? "" : host.getDescription());
        isActiveCheckbox.setValue(isActiveCheckbox.getValue());

        dialogLayout.add(ipField, descriptionField, isActiveCheckbox);

        Button saveButton = new Button("Сохранить", e -> {
            //можно заменить "" на null и обратно
            String ip = ipField.isEmpty() ? null : ipField.getValue();
            Integer isActive = isActiveCheckbox.getValue() ? 1 : 0;
            String description = descriptionField.isEmpty() ? null : descriptionField.getValue();
            if (ip == null) {
                Notification.show("Ошибка: SetID не может быть пустым", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                editHost(hostRepository, host.getId(), ip, isActive, description);

                Notification.show("Запись успешно изменена", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (NumberFormatException exception) {
                Notification.show(exception.toString(), 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            refreshGrid(hostRepository, dataProvider);
            dialog.close();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Отмена", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(saveButton, cancelButton);

        Button editHostButton = new Button("✏\uFE0F", e -> {
            dialog.open();
        });

        add(dialog);

        return editHostButton;
    }

    private Button deleteHostButton(HostRepository hostRepository, HostEntity host) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete entity");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        Text text = new Text("Подтвердите удаление маршрута");

        dialogLayout.add(text);

        Button deleteButton = new Button("Удалить", e -> {
            deleteHost(hostRepository, host.getId());
            refreshGrid(hostRepository, dataProvider);
//            grid.setItems(hostRepository.findAll(Sort.by("id")));
            dialog.close();
            Notification.show("Запись успешно удалена", 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        Button cancelButton = new Button("Отмена", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(deleteButton, cancelButton);

        Button deleteHostButton = new Button("❌", e -> {
            dialog.open();
        });

        add(dialog);

        return deleteHostButton;
    }

    private void customizeFields(
            TextField ipField,
            TextField descriptionField
    ) {
        ipField.setHelperText("172.27.x.x");
    }

    private void refreshGrid(HostRepository hostRepository, ListDataProvider<HostEntity> dataProvider) {
        List<HostEntity> updatedItems = hostRepository.findAll(Sort.by("id"));
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(updatedItems);
        dataProvider.refreshAll();
    }
}
