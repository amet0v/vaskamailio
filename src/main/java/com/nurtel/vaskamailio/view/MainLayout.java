package com.nurtel.vaskamailio.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.server.StreamResource;

import java.util.List;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createSidebar();
    }

    private void createHeader() {
        StreamResource imageResource = new StreamResource("logo_o.svg",
                () -> getClass().getResourceAsStream("/images/logo_o.svg"));

        Image image = new Image(imageResource, "My Streamed Image");
        image.setWidth("70px");
        image.setHeight("70px");

//        H1 logo = new H1("«NUR Telecom» LLC");
        H1 logo = new H1("VAS-Kamailio");
        logo.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("color", "#ffffff");

        HorizontalLayout nurLogo = new HorizontalLayout();
        nurLogo.setSpacing(false);
        nurLogo.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        nurLogo.add(image, logo);

        HorizontalLayout header = new HorizontalLayout(nurLogo);
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

    private void createSidebar() {
        SideNav sideNav = new SideNav();

        Icon routerIcon = VaadinIcon.DATABASE.create();
        routerIcon.setColor("#b8c7ce");
        SideNavItem routerItem = new SideNavItem("Router", RouterView.class, routerIcon);

        Icon dispatcherIcon = VaadinIcon.SCALE.create();
        dispatcherIcon.setColor("#b8c7ce");
        SideNavItem dispatcherItem = new SideNavItem("Dispatcher", DispatcherView.class, dispatcherIcon);

        List<SideNavItem> sideNavItems = List.of(routerItem, dispatcherItem);

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
