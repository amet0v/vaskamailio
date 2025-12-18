package com.nurtel.vaskamailio.view;

import com.nurtel.vaskamailio.audit.entity.AuditEntity;
import com.nurtel.vaskamailio.audit.repository.AuditRepository;
import com.nurtel.vaskamailio.cdr.entity.CdrEntity;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Route(value = "/audit", layout = MainLayout.class)
@PageTitle("Kamailio | Audit")
public class AuditView extends VerticalLayout {
    private final AuditRepository auditRepository;

    public AuditView(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;

        Boolean isAllow = MainLayout.isAllow();
        if (!isAllow) {
            Text notAllowedText = new Text("Просмотр страницы недоступен");
            add(notAllowedText);
            return;
        }

        Grid<AuditEntity> grid = new Grid<>(AuditEntity.class, false);
        grid.setHeight("75vh");

        grid.addColumn(AuditEntity::getDate)
                .setHeader("DateTime")
                .setWidth("20%")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        grid.addColumn(AuditEntity::getUsername)
                .setHeader("Username")
                .setWidth("10%")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        grid.addColumn(AuditEntity::getServer)
                .setHeader("Server")
                .setWidth("10%")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        grid.addColumn(AuditEntity::getAction)
                .setHeader("Action")
                .setWidth("10%")
                .setFlexGrow(0)
                .setSortable(true)
                .setResizable(true);

        grid.addColumn(AuditEntity::getDescription)
                .setHeader("Description")
                .setSortable(true)
                .setResizable(true)
                .setWidth("1500px")
                .setFlexGrow(0);

        grid.setItems();

        HorizontalLayout filterLayout = new HorizontalLayout();

        DateTimePicker startDateTimePicker = new DateTimePicker("От");
        startDateTimePicker.setValue(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0));
        startDateTimePicker.setWidth("250px");

        DateTimePicker endDateTimePicker = new DateTimePicker("До");
        endDateTimePicker.setValue(LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999));
        endDateTimePicker.setWidth("250px");

        startDateTimePicker.setLocale(Locale.of("ru", "RU"));
        endDateTimePicker.setLocale(Locale.of("ru", "RU"));

        TextField usernameField = new TextField("Username");
        TextField serverField = new TextField("Server");
        TextField actionField = new TextField("Action");
        TextField descriptionField = new TextField("Description");

        Button selectButton = new Button("Поиск", e -> {
            if (startDateTimePicker.isEmpty() && endDateTimePicker.isEmpty()) {
                Notification.show("Укажите временной промежуток для поиска CDR ", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }

            List<AuditEntity> items = auditRepository.findByFieldsInTimeRange(
                    startDateTimePicker.getValue(),
                    endDateTimePicker.getValue(),
                    usernameField.isEmpty() ? "" : usernameField.getValue(),
                    serverField.isEmpty() ? "" : serverField.getValue(),
                    actionField.isEmpty() ? "" : actionField.getValue(),
                    descriptionField.isEmpty() ? "" : descriptionField.getValue()
            );
            System.out.println(startDateTimePicker.getValue());
            System.out.println(endDateTimePicker.getValue());
            grid.setItems(items);
        });
        selectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        selectButton.getStyle().set("margin-top", "35px");

        filterLayout.add(startDateTimePicker, endDateTimePicker, usernameField, serverField, actionField, descriptionField, selectButton);

        add(filterLayout);

        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setWidthFull();

        add(grid);
    }
}
