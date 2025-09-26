package com.nurtel.vaskamailio.view;

import com.nurtel.vaskamailio.db.config.DatabaseContextHolder;
import com.nurtel.vaskamailio.dispatcher.entity.DispatcherEntity;
import com.nurtel.vaskamailio.prefix.entity.PrefixEntity;
import com.nurtel.vaskamailio.prefix.repository.PrefixRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.nurtel.vaskamailio.prefix.service.PrefixService.*;

@Route(value = "/prefixes", layout = MainLayout.class)
@PageTitle("Kamailio | Prefixes")
public class PrefixView extends VerticalLayout {
    private final PrefixRepository prefixRepository;
    private ListDataProvider<PrefixEntity> dataProvider = new ListDataProvider<>(new ArrayList<>());
    private Grid<PrefixEntity> prefixEntityGrid;
    public static Button addButton = new Button();

    public PrefixView(PrefixRepository prefixRepository) {
        this.prefixRepository = prefixRepository;

        Boolean isAllow = MainLayout.isAllow();
        if (!isAllow) {
            Text notAllowedText = new Text("Просмотр страницы недоступен");
            add(notAllowedText);
            return;
        }

        prefixEntityGrid = new Grid<>(PrefixEntity.class, false);
        prefixEntityGrid.getStyle().set("height", "80vh");

        addButton = createPrefixButton(prefixRepository);

        setupDbContext();

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

        prefixEntityGrid.addColumn(PrefixEntity::getRegex)
                .setHeader("Prefix regex")
//                .setWidth("20%")
//                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        prefixEntityGrid.addColumn(PrefixEntity::getSetid)
                .setHeader("SetID")
//                .setWidth("20%")
//                .setFlexGrow(0)
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

        prefixEntityGrid.addColumn(PrefixEntity::getDescription)
                .setHeader("Описание")
                .setSortable(true)
                .setResizable(true);

        prefixEntityGrid.addComponentColumn(prefixEntity -> {
                    Button editButton = editPrefixButton(prefixRepository, prefixEntity);
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
                    Button deleteButton = deletePrefixButton(prefixRepository, prefixEntity);
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
                    return (prefix.getRegex() != null && prefix.getRegex().toLowerCase().contains(value))
                            || (prefix.getDescription() != null && prefix.getDescription().toLowerCase().contains(value));
                }));
        return filterField;
    }

    private Button createPrefixButton(PrefixRepository prefixRepository) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("New entity");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        TextField regexField = new TextField("Prefix regex");
        IntegerField setidField = new IntegerField("SetID");
        Checkbox stripCheckbox = new Checkbox("Убрать префикс?", false);
        TextField descriptionField = new TextField("Description");

//        customizeFields(didField, setidField, descriptionField);

        dialogLayout.add(regexField, setidField, stripCheckbox, descriptionField);

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
                setupDbContext();
                createPrefix(prefixRepository, regex, setid, stripCheckbox.getValue(), description);
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

        Button addPrefixButton = new Button("Добавить", e -> {
            descriptionField.clear();
            dialog.open();
        });
        addPrefixButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(dialog);

        return addPrefixButton;
    }

    private Button editPrefixButton(PrefixRepository prefixRepository, PrefixEntity prefix) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit entity");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        TextField regexField = new TextField("Prefix regex");
        IntegerField setidField = new IntegerField("SetID");
        Checkbox stripCheckbox = new Checkbox("Убрать префикс?", false);
        TextField descriptionField = new TextField("Description");

//        customizeFields(didField, setidField, descriptionField);

        regexField.setValue(prefix.getRegex() == null ? "" : prefix.getRegex());
        setidField.setValue(prefix.getSetid() == null ? 0 : prefix.getSetid());
        stripCheckbox.setValue(prefix.getStrip());
        descriptionField.setValue(prefix.getDescription() == null ? "" : prefix.getDescription());

        dialogLayout.add(regexField, setidField, stripCheckbox, descriptionField);

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
                setupDbContext();
                editPrefix(prefixRepository, prefix.getId(), regex, setid, stripCheckbox.getValue(), description);

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

        Button editPrefixButton = new Button("✏\uFE0F", e -> {
            dialog.open();
        });

        add(dialog);

        return editPrefixButton;
    }

    private Button deletePrefixButton(PrefixRepository prefixRepository, PrefixEntity prefix) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Delete entity");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialog.add(dialogLayout);

        Text text = new Text("Подтвердите удаление маршрута");

        dialogLayout.add(text);

        Button deleteButton = new Button("Удалить", e -> {
            setupDbContext();
            deletePrefix(prefixRepository, prefix.getId());
            refreshGrid();
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
        List<PrefixEntity> updatedItems = prefixRepository.findAll(Sort.by("id"));
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(updatedItems);
        dataProvider.refreshAll();
    }
}
