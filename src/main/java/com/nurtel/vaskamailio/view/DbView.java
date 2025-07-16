package com.nurtel.vaskamailio.view;

import com.nurtel.vaskamailio.db.config.DatabaseContextHolder;
import com.nurtel.vaskamailio.db.entity.DbEntity;
import com.nurtel.vaskamailio.db.repository.DbRepository;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Route(value = "db", layout = MainLayout.class)
@PageTitle("Kamailio | Databases")
public class DbView extends VerticalLayout {
    private final DbRepository dbRepository;
    private ListDataProvider<DbEntity> dataProvider = new ListDataProvider<>(new ArrayList<>());
    private Grid<DbEntity> dbEntityGrid;
    public static Button addButton = new Button();
    public DbView(DbRepository dbRepository) {
        this.dbRepository = dbRepository;

        Boolean isAllow = MainLayout.isAllow();
        if (!isAllow) {
            Text notAllowedText = new Text("Просмотр страницы недоступен");
            add(notAllowedText);
            return;
        }

        setupDbContext();

        dbEntityGrid = new Grid<>(DbEntity.class, false);
        dbEntityGrid.getStyle().set("height", "80vh");

//        addButton = createDbButton(dbRepository);
        addButton = new Button();

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

        horizontalLayout.add(addButton, info, filterField);
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
                .setHeader("Asterisk Socket")
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
//                    Button editButton = editDbButton(dbRepository, dbEntity);
                    Button editButton = new Button();
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
//                    Button deleteButton = deleteDbButton(dbRepository, dbEntity);
                    Button deleteButton = new Button();
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

    private TextField getFilterField() {
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
        List<DbEntity> updatedItems = dbRepository.findAll(Sort.by("id"));
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(updatedItems);
        dataProvider.refreshAll();
    }
}
