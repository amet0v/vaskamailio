package com.nurtel.vaskamailio.view;

import com.nurtel.vaskamailio.audit.repository.AuditRepository;
import com.nurtel.vaskamailio.db.config.DatabaseContextHolder;
import com.nurtel.vaskamailio.dispatcher.entity.DispatcherEntity;
import com.nurtel.vaskamailio.dispatcher.repository.DispatcherRepository;
import com.nurtel.vaskamailio.prefix.entity.PrefixEntity;
import com.nurtel.vaskamailio.prefix.repository.PrefixRepository;
import com.vaadin.flow.component.ComponentUtil;
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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.data.domain.Sort;

import java.util.*;
import java.util.stream.Collectors;

import static com.nurtel.vaskamailio.audit.service.AuditService.addAuditEntity;
import static com.nurtel.vaskamailio.prefix.service.PrefixService.*;

@Route(value = "/regex", layout = MainLayout.class)
@PageTitle("Kamailio | RegEx")
public class PrefixView extends VerticalLayout {
    private final PrefixRepository prefixRepository;
    private final AuditRepository auditRepository;
    private ListDataProvider<PrefixEntity> dataProvider = new ListDataProvider<>(new ArrayList<>());
    private Grid<PrefixEntity> prefixEntityGrid;
    public static Button addButton = new Button();
    Map<Integer, String> setidToDescription;

    public PrefixView(PrefixRepository prefixRepository, DispatcherRepository dispatcherRepository, AuditRepository auditRepository) {
        this.prefixRepository = prefixRepository;
        this.auditRepository = auditRepository;

        Boolean isAllow = MainLayout.isAllow();
        if (!isAllow) {
            Text notAllowedText = new Text("Просмотр страницы недоступен");
            add(notAllowedText);
            return;
        }

        prefixEntityGrid = new Grid<>(PrefixEntity.class, false);
        prefixEntityGrid.getStyle().set("height", "80vh");

        addButton = createPrefixButton(prefixRepository, auditRepository, dispatcherRepository);

        setupDbContext(dispatcherRepository);

        List<PrefixEntity> items = prefixRepository.findAll(Sort.by("id"));
        dataProvider = new ListDataProvider<>(items);
        prefixEntityGrid.setDataProvider(dataProvider);

        TextField filterField = getFilterField();

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidthFull();
        horizontalLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        horizontalLayout.add(addButton, filterField);

        add(horizontalLayout);

        add(prefixEntityGrid);

        prefixEntityGrid.addColumn(PrefixEntity::getId)
                .setHeader("ID")
                .setWidth("5%")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        prefixEntityGrid.addColumn(PrefixEntity::getPattern)
                .setHeader("RegEx")
//                .setWidth("20%")
//                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        prefixEntityGrid.addColumn(e -> {
                    String desc = setidToDescription.get(e.getSetid());

                    if (desc != null) {
                        desc = desc.replaceAll("[_\\d]", "");
                    }

                    return desc == null
                            ? String.valueOf(e.getSetid())
                            : desc + " (setid: " + e.getSetid() + ")";
                })
                .setHeader("SetID")
                .setWidth("20%")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        prefixEntityGrid.addComponentColumn(entity -> {
                    Checkbox checkbox = new Checkbox();
                    checkbox.setValue(entity.getStrip());
                    checkbox.setReadOnly(true); // Чтобы пользователь не мог менять значение
                    return checkbox;
                }).setHeader("Убрать префикс?")
                .setSortable(true)
                .setResizable(true);

        prefixEntityGrid.addColumn(PrefixEntity::getStripChars)
                .setHeader("Кол-во символов")
                .setSortable(true)
                .setResizable(true);

        prefixEntityGrid.addColumn(PrefixEntity::getDescription)
                .setHeader("Описание")
                .setSortable(true)
                .setResizable(true);

        prefixEntityGrid.addComponentColumn(prefixEntity -> {
                    Button editButton = editPrefixButton(prefixRepository, prefixEntity, auditRepository, dispatcherRepository);
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

        prefixEntityGrid.addComponentColumn(prefixEntity -> {
                    Button deleteButton = deletePrefixButton(prefixRepository, prefixEntity, auditRepository, dispatcherRepository);
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

        prefixEntityGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        prefixEntityGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    private TextField getFilterField() {
        TextField filterField = new TextField();
        filterField.setPlaceholder("Поиск...");
        filterField.setPrefixComponent(new Icon("lumo", "search"));
        filterField.setClearButtonVisible(true);
        filterField.setWidth("300px");
        filterField.addValueChangeListener(e ->
                dataProvider.setFilter(prefix -> {
                    String value = e.getValue().toLowerCase();
                    return (prefix.getPattern() != null && prefix.getPattern().toLowerCase().contains(value))
                            || (prefix.getDescription() != null && prefix.getDescription().toLowerCase().contains(value));
                }));
        return filterField;
    }

    private Button createPrefixButton(
            PrefixRepository prefixRepository,
            AuditRepository auditRepository,
            DispatcherRepository dispatcherRepository
    ) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("New entity");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        TextField regexField = new TextField("Prefix regex");
        IntegerField setidField = new IntegerField("SetID");
        Checkbox stripCheckbox = new Checkbox("Убрать префикс?", false);
        IntegerField stripCharsField = new IntegerField("Кол-во символов");
        TextField descriptionField = new TextField("Описание");

        setidField.setStepButtonsVisible(true);
        setidField.setValue(0);
        setidField.setMin(0);
        stripCharsField.setStepButtonsVisible(true);
        stripCharsField.setValue(0);
        stripCharsField.setMin(0);
//        customizeFields(didField, setidField, descriptionField);

        dialogLayout.add(regexField, setidField, stripCheckbox, stripCharsField, descriptionField);

        Button saveButton = new Button("Сохранить", e -> {
            //можно заменить "" на null и обратно
            String regex = regexField.isEmpty() ? null : regexField.getValue();
            Integer setid = setidField.getValue();
            String description = descriptionField.isEmpty() ? null : descriptionField.getValue();
            if (regex == null) {
                Notification.show("Ошибка: regex не может быть пустым", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                setupDbContext(dispatcherRepository);
                PrefixEntity result = createPrefix(prefixRepository, regex, setid, stripCheckbox.getValue(), stripCharsField.getValue(), description);
                addAuditEntity(auditRepository, "ADD", result.toString());

                Notification.show("Запись успешно создана", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (RuntimeException exception) {
                Notification.show(exception.getMessage(), 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            refreshGrid(dispatcherRepository);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Отмена", e -> {
            dialog.close();
        });
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(saveButton, cancelButton);

        Button addPrefixButton = new Button("Добавить", e -> {
            descriptionField.clear();
            dialog.open();
        });
        addPrefixButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(dialog);

        return addPrefixButton;
    }

    private Button editPrefixButton(
            PrefixRepository prefixRepository,
            PrefixEntity prefix,
            AuditRepository auditRepository,
            DispatcherRepository dispatcherRepository
    ) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit entity");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        TextField regexField = new TextField("Prefix regex");
        IntegerField setidField = new IntegerField("SetID");
        Checkbox stripCheckbox = new Checkbox("Убрать префикс?", false);
        IntegerField stripCharsField = new IntegerField("Кол-во символов");
        TextField descriptionField = new TextField("Описание");

        setidField.setStepButtonsVisible(true);
        setidField.setValue(0);
        setidField.setMin(0);
        stripCharsField.setStepButtonsVisible(true);
        stripCharsField.setValue(0);
        stripCharsField.setMin(0);
//        customizeFields(didField, setidField, descriptionField);

        regexField.setValue(prefix.getPattern() == null ? "" : prefix.getPattern());
        setidField.setValue(prefix.getSetid() == null ? 0 : prefix.getSetid());
        stripCheckbox.setValue(prefix.getStrip());
        stripCharsField.setValue(prefix.getStripChars() == null ? 0 : prefix.getStripChars());
        descriptionField.setValue(prefix.getDescription() == null ? "" : prefix.getDescription());

        dialogLayout.add(regexField, setidField, stripCheckbox, stripCharsField, descriptionField);

        Button saveButton = new Button("Сохранить", e -> {
            //можно заменить "" на null и обратно
            String regex = regexField.isEmpty() ? null : regexField.getValue();
            Integer setid = setidField.getValue();
            String description = descriptionField.isEmpty() ? null : descriptionField.getValue();
            if (setid == null) {
                Notification.show("Ошибка: regex не может быть пустым", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                setupDbContext(dispatcherRepository);
                addAuditEntity(auditRepository, "EDIT (OLD)", prefix.toString());
                Optional<PrefixEntity> result = editPrefix(
                        prefixRepository, prefix.getId(), regex, setid, stripCheckbox.getValue(), stripCharsField.getValue(), description);
                result.ifPresent(prefixEntity -> addAuditEntity(auditRepository, "EDIT (NEW)", prefixEntity.toString()));

                Notification.show("Запись успешно изменена", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (RuntimeException exception) {
                Notification.show(exception.getMessage(), 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            refreshGrid(dispatcherRepository);
            dialog.close();
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Отмена", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(saveButton, cancelButton);

        Button editPrefixButton = new Button("✏\uFE0F", e -> {
            dialog.open();
        });

        add(dialog);

        return editPrefixButton;
    }

    private Button deletePrefixButton(
            PrefixRepository prefixRepository,
            PrefixEntity prefix,
            AuditRepository auditRepository,
            DispatcherRepository dispatcherRepository
    ) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete entity");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        Text text = new Text("Подтвердите удаление маршрута");

        dialogLayout.add(text);

        Button deleteButton = new Button("Удалить", e -> {
            setupDbContext(dispatcherRepository);
            addAuditEntity(auditRepository, "DELETE", prefix.toString());
            deletePrefix(prefixRepository, prefix.getId());
            refreshGrid(dispatcherRepository);
            dialog.close();
            Notification.show("Запись успешно удалена", 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Отмена", e -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        dialog.getFooter().add(deleteButton, cancelButton);

        Button deletePrefixButton = new Button("❌", e -> {
            dialog.open();
        });

        add(dialog);

        return deletePrefixButton;
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
    }

    private void refreshGrid(DispatcherRepository dispatcherRepository) {
//        setupDbContext(dispatcherRepository);
        List<PrefixEntity> updatedItems = prefixRepository.findAll(Sort.by("id"));
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(updatedItems);
        dataProvider.refreshAll();
    }
}
