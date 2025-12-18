package com.nurtel.vaskamailio.view;

import com.nurtel.vaskamailio.audit.repository.AuditRepository;
import com.nurtel.vaskamailio.db.config.DatabaseContextHolder;
import com.nurtel.vaskamailio.host.repository.HostRepository;
import com.nurtel.vaskamailio.host.entity.HostEntity;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.nurtel.vaskamailio.audit.service.AuditService.addAuditEntity;
import static com.nurtel.vaskamailio.host.service.HostService.*;

@Route(value = "/hosts", layout = MainLayout.class)
@PageTitle("Kamailio | Hosts")
public class HostView extends VerticalLayout {
    private final HostRepository hostRepository;
    private final AuditRepository auditRepository;
    private ListDataProvider<HostEntity> dataProvider = new ListDataProvider<>(new ArrayList<>());
    private Grid<HostEntity> hostEntityGrid;
    public static Button addButton = new Button();

    public HostView(HostRepository hostRepository, AuditRepository auditRepository) {
        this.hostRepository = hostRepository;
        this.auditRepository = auditRepository;

        Boolean isAllow = MainLayout.isAllow();
        if (!isAllow) {
            Text notAllowedText = new Text("Просмотр страницы недоступен");
            add(notAllowedText);
            return;
        }

        setupDbContext();

        hostEntityGrid = new Grid<>(HostEntity.class, false);
        hostEntityGrid.getStyle().set("height", "80vh");

        addButton = createHostButton(hostRepository, auditRepository);

        List<HostEntity> items = hostRepository.findAll(Sort.by("id"));
        dataProvider = new ListDataProvider<>(items);
        hostEntityGrid.setDataProvider(dataProvider);

        Div alert = new Div(new Text("⚠️ Данные синхронизированы с dispatcher. Изменения вручную — только в исключительных случаях! ⚠️"));
        alert.getElement().getThemeList().add("badge error");
        alert.getStyle().set("font-size", "16px");

        TextField filterField = getFilterField();

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        horizontalLayout.add(addButton, alert, filterField);
        add(horizontalLayout);

        add(hostEntityGrid);

        hostEntityGrid.addColumn(HostEntity::getId)
                .setHeader("ID")
                .setWidth("5%")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        hostEntityGrid.addColumn(HostEntity::getIp)
                .setHeader("IP")
                .setSortable(true)
                .setResizable(true);

//        hostEntityGrid.addColumn(HostEntity::getKeyType)
//                .setHeader("Key type")
//                .setWidth("10%")
//                .setFlexGrow(0)
//                .setSortable(true)
//                .setResizable(true);
//
//        hostEntityGrid.addColumn(HostEntity::getValueType)
//                .setHeader("Value type")
//                .setWidth("10%")
//                .setFlexGrow(0)
//                .setSortable(true)
//                .setResizable(true);

        hostEntityGrid.addColumn(HostEntity::getDescription)
                .setHeader("Описание")
                .setSortable(true)
                .setResizable(true);

        hostEntityGrid.addComponentColumn(entity -> {
                    Checkbox checkbox = new Checkbox();
                    checkbox.setValue(Objects.equals(entity.getIsActive(), "1"));
                    checkbox.setReadOnly(true); // Чтобы пользователь не мог менять значение
                    return checkbox;
                }).setHeader("Активно?")
                .setSortable(true)
                .setResizable(true);

        hostEntityGrid.addComponentColumn(HostEntity -> {
                    Button editButton = editHostButton(hostRepository, HostEntity, auditRepository);
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

        hostEntityGrid.addComponentColumn(HostEntity -> {
                    Button deleteButton = deleteHostButton(hostRepository, HostEntity, auditRepository);
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

//        hostEntityGrid.setItems(hostRepository.findAll(Sort.by("id")));

        hostEntityGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        hostEntityGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
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

    private Button createHostButton(HostRepository hostRepository, AuditRepository auditRepository) {
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
                setupDbContext();
                HostEntity result = createHost(hostRepository, ip, isActive, description);
                addAuditEntity(auditRepository, "ADD", result.toString());
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
            ipField.clear();
            descriptionField.clear();
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(saveButton, cancelButton);

        Button addRouteButton = new Button("Добавить", e -> {
            ipField.clear();
            descriptionField.clear();
            dialog.open();
        });
        addRouteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(dialog);

        return addRouteButton;
    }

    private Button editHostButton(HostRepository hostRepository, HostEntity host, AuditRepository auditRepository) {
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
                setupDbContext();
                addAuditEntity(auditRepository, "EDIT (OLD)", host.toString());
                Optional<HostEntity> result = editHost(hostRepository, host.getId(), ip, isActive, description);
                result.ifPresent(hostEntity -> addAuditEntity(auditRepository, "EDIT (NEW)", hostEntity.toString()));
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

        Button editHostButton = new Button("✏\uFE0F", e -> {
            dialog.open();
        });

        add(dialog);

        return editHostButton;
    }

    private Button deleteHostButton(HostRepository hostRepository, HostEntity host, AuditRepository auditRepository) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete entity");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        Text text = new Text("Подтвердите удаление маршрута");

        dialogLayout.add(text);

        Button deleteButton = new Button("Удалить", e -> {
            setupDbContext();
            addAuditEntity(auditRepository, "DELETE", host.toString());
            deleteHost(hostRepository, host.getId());
            refreshGrid();
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
        setupDbContext();
        List<HostEntity> items = hostRepository.findAll(Sort.by("id"));
        dataProvider = new ListDataProvider<>(items);
        hostEntityGrid.setDataProvider(dataProvider);
    }
}
