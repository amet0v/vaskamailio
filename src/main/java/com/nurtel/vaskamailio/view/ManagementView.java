package com.nurtel.vaskamailio.view;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.notification.Notification;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Route(value = "/management", layout = MainLayout.class)
@PageTitle("Kamailio | Management")
public class ManagementView extends VerticalLayout {

    private final String sshIp1;
    private final String sshIp2;
    private final String sshIp3;
    private final String sshLogin;
    private final String sshPassword;
    private final TextArea outputArea = new TextArea("Вывод консоли");

    public ManagementView(
            @Value("${ssh.ip1}") String sshIp1,
            @Value("${ssh.ip2}") String sshIp2,
            @Value("${ssh.ip3}") String sshIp3,
            @Value("${ssh.login}") String sshLogin,
            @Value("${ssh.password}") String sshPassword
    ) {
        this.sshIp1 = sshIp1;
        this.sshIp2 = sshIp2;
        this.sshIp3 = sshIp3;
        this.sshLogin = sshLogin;
        this.sshPassword = sshPassword;

        Boolean isAllow = MainLayout.isAllow();
        if (!isAllow) {
            add(new Text("Просмотр страницы недоступен"));
            return;
        }

        outputArea.setWidthFull();
        outputArea.setHeight("50vh");
        outputArea.setReadOnly(true);

        HorizontalLayout serversLayout = new HorizontalLayout();
        serversLayout.setWidthFull();
        serversLayout.setSpacing(true);

        serversLayout.add(
                createServerColumn("kamailio01", sshIp1),
                createServerColumn("kamailio02", sshIp2),
                createServerColumn("kamailio03", sshIp3)
        );

        add(serversLayout, outputArea);
    }

    private VerticalLayout createServerColumn(String name, String ip) {
        VerticalLayout column = new VerticalLayout();
        column.setPadding(false);
        column.setSpacing(true);
        column.setWidth("100%");
        column.getStyle().set("border", "1px solid #ccc");
        column.getStyle().set("padding", "10px");
        column.getStyle().set("flex", "1");

        column.add(new Html("<h4>" + name + "</h4>"));

//        column.add(new Button("Read cfg", VaadinIcon.FILE.create(),
//                e -> runCommandOn(ip, "cat /etc/kamailio/kamailio.cfg")));
        column.add(new Button("Reload routes", VaadinIcon.REFRESH.create(),
                e -> runCommandOn(ip, "sudo /usr/sbin/kamcmd htable.reload routes")));
        column.add(new Button("Reload hosts", VaadinIcon.REFRESH.create(),
                e -> runCommandOn(ip, "sudo /usr/sbin/kamcmd htable.reload hosts")));

        Button restartBtn = new Button("Restart kamailio", VaadinIcon.POWER_OFF.create(), e -> {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("⚠️ Подтвердите действие ⚠️");
            Html alert = new Html("<div style='font-size:16px;'> " +
                    "Выполнение команды <b>systemctl restart kamailio.service</b> на " + name +
                    "<br>повлияет на продуктивный трафик. Продолжить?</div>");
            dialog.add(alert);

            Button confirmButton = new Button("Перезагрузить", ev -> {
                dialog.close();
                runCommandOn(ip, "sudo /bin/systemctl restart kamailio.service");
            });

            Button cancelButton = new Button("Отмена", ev -> dialog.close());
            cancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
            dialog.getFooter().add(confirmButton, cancelButton);
            dialog.open();
        });

        column.add(restartBtn);

        column.add(new Button("Kamailio status", VaadinIcon.CHECK_CIRCLE_O.create(),
                e -> runCommandOn(ip, "sudo /bin/systemctl status kamailio.service")));

        return column;
    }

    private void runCommandOn(String host, String command) {
        outputArea.clear();
        StringBuilder combinedOutput = new StringBuilder("🔧 Выполняется команда: " + command + " на " + host + "\n\n");

        String remoteOutput = runRemoteCommandWithPassword(host, sshLogin, sshPassword, command);
        combinedOutput.append("🌐 Удалённый результат:\n").append(remoteOutput);

        outputArea.setValue(combinedOutput.toString());
    }

    private String runRemoteCommandWithPassword(String host, String user, String password, String command) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, 22);
            session.setPassword(password);

            // отключаем проверку known_hosts
            session.setConfig("StrictHostKeyChecking", "no");

            session.connect(10 * 1000); // 10 секунд

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.setErrStream(System.err);
            InputStream in = channel.getInputStream();

            channel.connect();

            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            channel.disconnect();
            session.disconnect();

            return output.toString();

        } catch (Exception e) {
            Notification.show("Ошибка при выполнении команды: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return "Ошибка SSH (пароль): " + e.getMessage();
        }
    }
}