package com.nurtel.vaskamailio.view;

import com.nurtel.vaskamailio.audit.repository.AuditRepository;
import com.nurtel.vaskamailio.db.entity.DbEntity;
import com.nurtel.vaskamailio.db.repository.DbRepository;
import com.nurtel.vaskamailio.db.service.DbService;
import com.nurtel.vaskamailio.dispatcher.repository.DispatcherRepository;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import javassist.NotFoundException;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static com.nurtel.vaskamailio.audit.service.AuditService.addAuditEntity;
import static com.nurtel.vaskamailio.db.service.DbService.*;

@Route(value = "db", layout = MainLayout.class)
@PageTitle("Kamailio | Databases")
public class DbView extends VerticalLayout {
    private final DbRepository dbRepository;
    private final DispatcherRepository dispatcherRepository;
    private final AuditRepository auditRepository;
    private ListDataProvider<DbEntity> dataProvider = new ListDataProvider<>(new ArrayList<>());
    private Grid<DbEntity> dbEntityGrid;

    public DbView(DbRepository dbRepository, DispatcherRepository dispatcherRepository, AuditRepository auditRepository) {
        this.dbRepository = dbRepository;
        this.dispatcherRepository = dispatcherRepository;
        this.auditRepository = auditRepository;

        Boolean isAllow = MainLayout.isAllow();
        if (!isAllow) {
            Text notAllowedText = new Text("Просмотр страницы недоступен");
            add(notAllowedText);
            return;
        }

        dbEntityGrid = new Grid<>(DbEntity.class, false);
        dbEntityGrid.getStyle().set("height", "80vh");

        Button addButton = createDbButton(dbRepository, auditRepository);
        Button copyDbButton = new Button("Копировать БД", e -> {
            Dialog dialog = new Dialog();

            List<DbEntity> dbList = dbRepository.findAllByOrderByIdAsc();

            ComboBox<DbEntity> sourceComboBox = new ComboBox<>("Источник");
            sourceComboBox.setItems(dbList);
            sourceComboBox.setItemLabelGenerator(DbEntity::getName); // или getIp

            ComboBox<DbEntity> targetComboBox = new ComboBox<>("Цель");
            targetComboBox.setItems(dbList);
            targetComboBox.setItemLabelGenerator(DbEntity::getName);

            Button runButton = new Button("Выполнить", click -> {
                DbEntity source = sourceComboBox.getValue();
                DbEntity target = targetComboBox.getValue();

                if (source == null || target == null) {
                    Notification.show("Выберите обе базы", 5000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
                if (source == target) {
                    Notification.show("Выберите разные базы", 5000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                String result = DbService.copyDb(source.getIp(), target.getIp());
                addAuditEntity(auditRepository, "COPY DB", result);
                Notification.show(result, 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);;
                try {
                    result = DbService.editAttrs(dbRepository, dispatcherRepository, target.getIp());
                    addAuditEntity(auditRepository, "COPY DB", result);
                    Notification.show(result, 5000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);;
                } catch (NotFoundException ex) {
                    Notification.show(ex.toString(), 5000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
                dialog.close();
            });

            dialog.add(sourceComboBox, targetComboBox, runButton);
            dialog.open();
        });

            List<DbEntity> items = dbRepository.findAll(Sort.by("id"));
            dataProvider = new ListDataProvider<>(items);
            dbEntityGrid.setDataProvider(dataProvider);

            Div info = new Div(new Text("ℹ\uFE0F Данные берутся из БД сервера, где работает приложение. Переключение БД не влияет на выборку. ℹ\uFE0F"));
            info.getElement().getThemeList().add("badge");
            info.getStyle().set("font-size", "16px");

            TextField filterField = getFilterField();

            HorizontalLayout horizontalLayout = new HorizontalLayout();
            horizontalLayout.setWidthFull();
            horizontalLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

            horizontalLayout.add(addButton, copyDbButton, info, filterField);
            add(horizontalLayout);

            add(dbEntityGrid);

            dbEntityGrid.addColumn(DbEntity::getId)
                    .setHeader("ID")
                    .setWidth("5%")
                    .setFlexGrow(0)
                    .setSortable(true)
                    .setResizable(true);

            dbEntityGrid.addColumn(DbEntity::getIp)
                    .setHeader("IP")
                    .setSortable(true)
                    .setResizable(true);

            dbEntityGrid.addColumn(DbEntity::getName)
                    .setHeader("Name")
                    .setSortable(true)
                    .setResizable(true);

            dbEntityGrid.addColumn(DbEntity::getMscSocket)
                    .setHeader("MSC socket")
                    .setSortable(true)
                    .setResizable(true);

            dbEntityGrid.addColumn(DbEntity::getAsteriskSocket)
                    .setHeader("Asterisk socket")
                    .setSortable(true)
                    .setResizable(true);

            dbEntityGrid.addColumn(DbEntity::getLogin)
                    .setHeader("Login")
                    .setSortable(true)
                    .setResizable(true);

            dbEntityGrid.addColumn(DbEntity::getPassword)
                    .setHeader("Password")
                    .setSortable(true)
                    .setResizable(true);

            dbEntityGrid.addComponentColumn(dbEntity -> {
                        Button editButton = editDbButton(dbRepository, dbEntity);
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

            dbEntityGrid.addComponentColumn(dbEntity -> {
                        Button deleteButton = deleteDbButton(dbRepository, dbEntity);
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

            dbEntityGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
            dbEntityGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        }

        private TextField getFilterField () {
            TextField filterField = new TextField();
            filterField.setPlaceholder("Поиск...");
            filterField.setPrefixComponent(new Icon("lumo", "search"));
            filterField.setClearButtonVisible(true);
            filterField.setWidth("300px");
            filterField.addValueChangeListener(e ->
                    dataProvider.setFilter(db -> {
                        String value = e.getValue().toLowerCase();
                        return (db.getName() != null && db.getName().toLowerCase().contains(value))
                                || (db.getIp() != null && db.getIp().toLowerCase().contains(value));
                    }));
            return filterField;
        }

        private Button createDbButton (DbRepository dbRepository, AuditRepository auditRepository){
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("New entity");

            VerticalLayout dialogLayout = new VerticalLayout();
            dialog.add(dialogLayout);

            TextField ipField = new TextField("IP");
            ipField.setHelperText("172.27.201.x");
            TextField nameField = new TextField("Name");
            TextField mscSocketField = new TextField("MSC socket");
            mscSocketField.setHelperText("172.16.x.x:5060");
            TextField asterSocketField = new TextField("Asterisk socket");
            asterSocketField.setHelperText("172.27.201.x:5060");
            TextField loginField = new TextField("Login");
            TextField passwordField = new TextField("Password");

            dialogLayout.add(ipField, nameField, mscSocketField, asterSocketField, loginField, passwordField);

            Button saveButton = new Button("Сохранить", e -> {
                String ip = ipField.isEmpty() ? null : ipField.getValue();
                if (ip == null) {
                    Notification.show("Ошибка: IP не может быть пустым", 5000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                try {
                    DbEntity result = createDb(
                            dbRepository,
                            ip,
                            nameField.getValue(),
                            mscSocketField.getValue(),
                            asterSocketField.getValue(),
                            loginField.getValue(),
                            passwordField.getValue()
                    );
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
                nameField.clear();
                mscSocketField.clear();
                asterSocketField.clear();
                dialog.close();
            });
            cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            dialog.getFooter().add(saveButton, cancelButton);

            Button addBdButton = new Button("Добавить", e -> {
                ipField.clear();
                nameField.clear();
                mscSocketField.clear();
                asterSocketField.clear();
                dialog.open();
            });
            addBdButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

            add(dialog);

            return addBdButton;
        }

        private Button editDbButton (DbRepository dbRepository, DbEntity db){
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Edit entity");

            VerticalLayout dialogLayout = new VerticalLayout();
            dialog.add(dialogLayout);

            TextField ipField = new TextField("IP");
            ipField.setHelperText("172.27.201.x");
            TextField nameField = new TextField("Name");
            TextField mscSocketField = new TextField("MSC socket");
            mscSocketField.setHelperText("172.16.x.x:5060");
            TextField asterSocketField = new TextField("Asterisk socket");
            asterSocketField.setHelperText("172.27.201.x:5060");
            TextField loginField = new TextField("Login");
            TextField passwordField = new TextField("Password");

            ipField.setValue(db.getIp() == null ? "" : db.getIp());
            nameField.setValue(db.getName() == null ? "" : db.getName());
            mscSocketField.setValue(db.getMscSocket() == null ? "" : db.getMscSocket());
            asterSocketField.setValue(db.getAsteriskSocket() == null ? "" : db.getAsteriskSocket());
            loginField.setValue(db.getLogin() == null ? "" : db.getLogin());
            passwordField.setValue(db.getPassword() == null ? "" : db.getPassword());

            dialogLayout.add(ipField, nameField, mscSocketField, asterSocketField, loginField, passwordField);

            Button saveButton = new Button("Сохранить", e -> {
                String ip = ipField.isEmpty() ? null : ipField.getValue();
                if (ip == null) {
                    Notification.show("Ошибка: IP не может быть пустым", 5000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }

                try {
                    addAuditEntity(auditRepository, "EDIT (OLD)", db.toString());
                    DbEntity result = editDb(
                            dbRepository,
                            db.getId(),
                            ip,
                            nameField.getValue(),
                            mscSocketField.getValue(),
                            asterSocketField.getValue(),
                            loginField.getValue(),
                            passwordField.getValue()
                    );
                    addAuditEntity(auditRepository, "EDIT (NEW)", result.toString());
                    Notification.show("Запись успешно изменена", 5000, Notification.Position.BOTTOM_END)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } catch (NotFoundException | NumberFormatException exception) {
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

        private Button deleteDbButton (DbRepository dbRepository, DbEntity db){
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Delete entity");

            VerticalLayout dialogLayout = new VerticalLayout();
            dialog.add(dialogLayout);

            Text text = new Text("Подтвердите удаление маршрута");

            dialogLayout.add(text);

            Button deleteButton = new Button("Удалить", e -> {
                addAuditEntity(auditRepository, "DELETE", db.toString());
                deleteDb(dbRepository, db.getId());
                refreshGrid();
                dialog.close();
                Notification.show("Запись успешно удалена", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
            Button cancelButton = new Button("Отмена", e -> dialog.close());
            cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            dialog.getFooter().add(deleteButton, cancelButton);

            Button deleteDbButton = new Button("❌", e -> {
                dialog.open();
            });

            add(dialog);

            return deleteDbButton;
        }

        private void refreshGrid () {
            List<DbEntity> updatedItems = dbRepository.findAll(Sort.by("id"));
            dataProvider.getItems().clear();
            dataProvider.getItems().addAll(updatedItems);
            dataProvider.refreshAll();
        }
    }
