package com.nurtel.vaskamailio.db.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.nurtel.vaskamailio.db.repository", // путь к твоим нужным репозиториям
        entityManagerFactoryRef = "dbEntityManagerFactory",
        transactionManagerRef = "databasesTransactionManager"
)
public class DatabasesJpaConfig {
}

