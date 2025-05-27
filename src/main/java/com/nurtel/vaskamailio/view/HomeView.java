package com.nurtel.vaskamailio.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Kamailio | Home")
public class HomeView extends Div {
    public HomeView() {
        add("Добро пожаловать на главную страницу!");
    }
}