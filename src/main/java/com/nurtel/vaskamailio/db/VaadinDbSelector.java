package com.nurtel.vaskamailio.db;

import com.vaadin.flow.server.*;

import org.springframework.stereotype.Component;

@Component
public class VaadinDbSelector implements VaadinServiceInitListener {
    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.addRequestHandler((session, request, response) -> {
            session.access(() -> {
                String dbKey = (String) session.getAttribute("selectedDb");
                DatabaseContextHolder.set(dbKey != null ? dbKey : "kamailio01");
            });
            return false;
        });
    }
}
