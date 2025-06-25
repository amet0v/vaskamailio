package com.nurtel.vaskamailio.view;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchScope;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.List;

public class MainLayout extends AppLayout {
    @Autowired
    private AuthenticationManager authenticationManager;
    private final Integer sessionInterval = 3600;

    private Button loginButton;
    private MenuBar logoutBar;

    public static Boolean isAllow() {
        String departmentString = "Группа управления VAS-платформами";
        return departmentString.equals(
                String.valueOf(VaadinSession.getCurrent().getAttribute("department"))
        );
    }

    public static Boolean isAuth() {
        return VaadinSession.getCurrent().getAttribute("username") != null;
    }

    public MainLayout() {
        createHeader();
        createSidebar();
    }

    private void createHeader() {
        StreamResource imageResource = new StreamResource("logo_o.svg",
                () -> getClass().getResourceAsStream("/images/logo_o.svg"));

        Image image = new Image(imageResource, "NurTelecom");
        image.setWidth("70px");
        image.setHeight("70px");

        H1 logo = new H1("VAS-Kamailio");
        logo.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("color", "#ffffff");

        HorizontalLayout nurLogo = new HorizontalLayout();
        nurLogo.setSpacing(false);
        nurLogo.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        nurLogo.add(image, logo);

        loginButton = createLoginButton();

        //copypaste
        Button logoutButton = new Button("Выйти", VaadinIcon.POWER_OFF.create(), e -> {
            VaadinSession.getCurrent().getSession().invalidate();
            VaadinSession.getCurrent().close();
            Notification.show("Вы вышли из учетной записи", 3000, Notification.Position.BOTTOM_END);
            UI.getCurrent().getPage().reload();
        });
        logoutButton.getStyle()
                .set("background-color", "#ff2898")
                .set("color", "#ffffff");

        logoutBar = new MenuBar();
        Icon userIcon = new Icon(VaadinIcon.USER);

        MenuItem usernameItem = logoutBar.addItem(userIcon);
        usernameItem.getSubMenu().addItem(logoutButton);

        String username = "";
        if (VaadinSession.getCurrent().getAttribute("username") != null)
            username = VaadinSession.getCurrent().getAttribute("username").toString();

        usernameItem.add(username);
        usernameItem.getStyle()
                .set("background-color", "#ff2898")
                .set("color", "#ffffff");

        logoutBar.setVisible(isAuth());
        logoutBar.getStyle()
                .set("background-color", "#ff2898")
                .set("color", "#ffffff");



        HorizontalLayout header = new HorizontalLayout(nurLogo, loginButton, logoutBar);
        //end of copypaste
//        HorizontalLayout header = new HorizontalLayout(nurLogo, loginButton);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.setPadding(false);
        header.setSpacing(true);

        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        header.getStyle()
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
                .set("background-color", "#ef107f")
                .set("color", "#000000")
                .set("padding", "0.2%")
                //.set("padding-bottom", "0.5%")
                .set("padding-right", "1%")
                .set("margin", "0");

        addToNavbar(header);
    }

    private Button createLoginButton() {
        Dialog loginDialog = new Dialog();

        loginDialog.setHeaderTitle("Вход в систему");
        TextField usernameField = new TextField("Логин");
        PasswordField passwordField = new PasswordField("Пароль");

        VerticalLayout loginDialogLayout = new VerticalLayout();
        loginDialogLayout.add(usernameField, passwordField);

        loginDialog.add(loginDialogLayout);

        Button dialogLoginButton = new Button("Войти", e -> {
            String username = usernameField.getValue();
            String password = passwordField.getValue();

            // LDAP start
            try {
                Authentication auth = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(username, password)
                );

                VaadinSession.getCurrent().setAttribute("username", username);
                VaadinSession.getCurrent().setAttribute("department", getDepartmentFromLDAP(username));

                WrappedSession wrappedSession = VaadinSession.getCurrent().getSession();
                wrappedSession.setMaxInactiveInterval(sessionInterval);

                loginDialog.close();

                UI.getCurrent().getPage().reload();

                Notification.show("Сессия установлена для пользователя: " + username, 5000, Notification.Position.BOTTOM_END);
            } catch (AuthenticationException ex) {
                System.out.println("LDAP error info:");
                System.out.println(ex);
                System.out.println("Ошибка авторизации пользователя: " + username);
//                System.out.println("Password raw: [" + password + "]");
//                System.out.println("Password length: " + password.length());

                Notification.show("Неверные учетные данные", 5000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
            //LDAP end
        });
        dialogLoginButton.addClickShortcut(Key.ENTER);

        Button loginDialogCancelButton = new Button("Отмена", e -> loginDialog.close());
        loginDialogCancelButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        loginDialog.getFooter().add(dialogLoginButton, loginDialogCancelButton);

        Button loginButton = new Button("Войти", e -> {
            usernameField.clear();
            passwordField.clear();
            loginDialog.open();
        });
        loginButton.setVisible(!isAuth());
        loginButton.getStyle()
                .set("background-color", "#ff2898")
                .set("color", "#ffffff");

        return loginButton;
    }

    public static String getDepartmentFromLDAP(String username) {
        try {
            // Параметры подключения
            String ldapHost = "172.27.129.4";
            int ldapPort = 389;
            String bindDN = "cn=ldap_dutyschedule, ou=LDAP,ou=Service Accounts,dc=in,dc=nurtelecom,dc=kg";
            String bindPassword = "w6abd#hm9sW!RBYSzAHF";
            String baseDN = "ou=Дирекция информационных технологий,ou=Генеральная дирекция,dc=in,dc=nurtelecom,dc=kg";

            // Подключение
            LDAPConnection conn = new LDAPConnection(ldapHost, ldapPort, bindDN, bindPassword);

            String filter = "(mailNickname=" + username + ")";
            SearchRequest request = new SearchRequest(baseDN, SearchScope.SUB, filter, "department");

            SearchResult result = conn.search(request);

            if (!result.getSearchEntries().isEmpty()) {
                return result.getSearchEntries().getFirst().getAttributeValue("department");
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void createSidebar() {
        SideNav sideNav = new SideNav();

        Icon routerDidIcon = VaadinIcon.ARROW_FORWARD.create();
        routerDidIcon.setColor("#b8c7ce");
        SideNavItem routerDidItem = new SideNavItem("Router", RouterView.class, routerDidIcon);

        Icon dispatcherIcon = VaadinIcon.SCALE.create();
        dispatcherIcon.setColor("#b8c7ce");
        SideNavItem dispatcherItem = new SideNavItem("Dispatcher", DispatcherView.class, dispatcherIcon);

        Icon hostsIcon = VaadinIcon.LIST_UL.create();
        hostsIcon.setColor("#b8c7ce");
        SideNavItem hostsItem = new SideNavItem("Hosts", HostView.class, hostsIcon);

        Icon managementIcon = VaadinIcon.SERVER.create();
        managementIcon.setColor("#b8c7ce");
        SideNavItem managementItem = new SideNavItem("Управление", ManagementView.class, managementIcon);

        List<SideNavItem> sideNavItems = List.of(routerDidItem, dispatcherItem, hostsItem, managementItem);

        for (SideNavItem item : sideNavItems) {
            item.getStyle()
                    .set("color", "#b8c7ce")
                    .set("font-size", "14px")
                    .set("font-weight", "400")
                    .set("padding-bottom", "10px")
                    .set("padding-top", "10px");

            item.addAttachListener(event -> {
                item.getElement().addEventListener("mouseover", e ->
                        item.getElement().getStyle()
                                .set("background-color", "#394247")
                                .set("color", "#ffffff")
                );
                item.getElement().addEventListener("mouseout", e ->
                        item.getElement().getStyle()
                                .set("background-color", "")
                                .set("color", "#b8c7ce")
                );
            });
        }

        sideNav.getStyle()
                .set("background-color", "#232b33");

        for (SideNavItem item : sideNavItems) {
            sideNav.addItem(item);
        }
        addToDrawer(sideNav);

        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setSpacing(true);
        sidebar.setPadding(true);
        sidebar.getStyle()
                .set("background-color", "#232b33")
                .set("color", "#b8c7ce")
                .set("height", "100vh");

        addToDrawer(sidebar);
    }
}
