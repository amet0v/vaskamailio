package com.nurtel.vaskamailio.view;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.notification.Notification;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Route(value = "/management", layout = MainLayout.class)
@PageTitle("Kamailio | Management")
public class ManagementView extends VerticalLayout {

    private final TextArea outputArea = new TextArea("Вывод консоли");

    public ManagementView() {

        outputArea.setWidthFull();
        outputArea.setHeight("50vh");
        outputArea.setReadOnly(true);

        Button readKamailioCfgBtn = new Button("Read kamailio.cfg", VaadinIcon.FILE.create());
        Button reloadRoutesBtn = new Button("Reload htable routes", VaadinIcon.REFRESH.create());
        Button reloadHostsBtn = new Button("Reload htable hosts", VaadinIcon.REFRESH.create());
        Button restartKamailioBtn = new Button("Restart kamailio.service", VaadinIcon.POWER_OFF.create());

        readKamailioCfgBtn.addClickListener(e -> runCommand("cat /etc/kamailio/kamailio.cfg"));
        reloadRoutesBtn.addClickListener(e -> runCommand("kamcmd htable.reload routes"));
        reloadHostsBtn.addClickListener(e -> runCommand("kamcmd htable.reload hosts"));
        restartKamailioBtn.addClickListener(e -> {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("⚠️ Подтвердите действие ⚠️");

            Html alert = new Html("<div style='font-size:16px;'> " +
                    "Выполнение команды <b>systemctl restart kamailio.service</b>" +
                    "<br>повлияет на продуктивный трафик. Продолжить?</div>");
            dialog.add(alert);

            Button confirmButton = new Button("Перезагрузить", ev -> {
                dialog.close();
                runCommand("systemctl restart kamailio.service");
            });

            Button cancelButton = new Button("Отмена", ev -> dialog.close());
            cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

            dialog.getFooter().add(confirmButton, cancelButton);

            dialog.open();
        });

        add(readKamailioCfgBtn, reloadRoutesBtn, reloadHostsBtn, restartKamailioBtn, outputArea);
    }

    private void runCommand(String command) {
        outputArea.clear();

        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("sh", "-c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            output.append("Выполнение команды: ").append(command).append("\n");
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            outputArea.setValue(output + "\nКоманда завершена с кодом: " + exitCode);

        } catch (Exception ex) {
            Notification.show("Ошибка при выполнении команды: " + ex.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}