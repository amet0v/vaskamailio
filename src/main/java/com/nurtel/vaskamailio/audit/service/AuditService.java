package com.nurtel.vaskamailio.audit.service;

import com.nurtel.vaskamailio.audit.entity.AuditEntity;
import com.nurtel.vaskamailio.audit.repository.AuditRepository;
import com.nurtel.vaskamailio.db.config.DatabaseContextHolder;
import com.nurtel.vaskamailio.view.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuditService {
    public static AuditEntity addAuditEntity(
            AuditRepository auditRepository,
            String action,
            String description
    ) {
        Optional<String> selectedDB = getSelectedDb();
        String mainDB = "kamailio-ui";
        DatabaseContextHolder.set(mainDB);

        AuditEntity audit = AuditEntity.builder()
                .date(LocalDateTime.now())
                .username((String) VaadinSession.getCurrent().getAttribute("username"))
                .server(selectedDB.orElse(null))
                .action(action)
                .description(description)
                .build();

        audit = auditRepository.save(audit);
        setupDbContext();
        return audit;
    }

    public static AuditEntity addAuditEntity(
            AuditRepository auditRepository,
            String username,
            String action,
            String description
    ) {
        Optional<String> selectedDB = getSelectedDb();
        String mainDB = "kamailio-ui";
        DatabaseContextHolder.set(mainDB);

        AuditEntity audit = AuditEntity.builder()
                .date(LocalDateTime.now())
                .username(username)
                .server(selectedDB.orElse(null))
                .action(action)
                .description(description)
                .build();

        audit = auditRepository.save(audit);
        setupDbContext();
        return audit;
    }

    private static void setupDbContext() {
        getSelectedDb().ifPresent(DatabaseContextHolder::set);
    }

    private static Optional<String> getSelectedDb() {
        return UI.getCurrent().getChildren()
                .filter(c -> c instanceof MainLayout)
                .map(c -> ((MainLayout) c).getDbSelector().getValue())
                .findFirst();
    }
}
