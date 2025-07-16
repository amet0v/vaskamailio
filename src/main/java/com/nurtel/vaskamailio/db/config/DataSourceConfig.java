package com.nurtel.vaskamailio.db.config;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url0}")
    private String url0;

    @Value("${spring.datasource.username}")
    private String login;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean(name = "ds0")
    public DataSource dataSource0() {
        return DataSourceBuilder.create()
                .url(url0)
                .username(login)
                .password(password)
                .build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(@Qualifier("databasesDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    public Map<Object, Object> loadAllDataSources(JdbcTemplate jdbcTemplate) {
        RoutingDataSource routingDataSource = new RoutingDataSource();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT name, ip, login, password FROM databases");

        Map<Object, Object> targetDataSources = new HashMap<>();
        for (Map<String, Object> row : rows) {
            DataSource ds = DataSourceBuilder.create()
                    .url("jdbc:postgresql://" + row.get("ip") + ":5432/kamailiodb")
                    .username((String) row.get("login"))
                    .password((String) row.get("password"))
                    .driverClassName("org.postgresql.Driver")
                    .build();
            targetDataSources.put(row.get("name"), ds);
        }
        return targetDataSources;
    }

    @Primary
    @Bean
    public DataSource dataSource(JdbcTemplate jdbcTemplate) {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        Map<Object, Object> targetDataSources = loadAllDataSources(jdbcTemplate);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(databasesDataSource());
        return routingDataSource;
    }

    @Primary
    @Bean(name = "mainEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mainEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("dataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.nurtel.vaskamailio") // исключая .db, если нужно
                .persistenceUnit("main")
                .build();
    }

    @Bean(name = "mainTransactionManager")
    public PlatformTransactionManager mainTransactionManager(
            @Qualifier("mainEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
    @Primary
    @Bean(name = "databasesDataSource")
    public DataSource databasesDataSource() {
        return DataSourceBuilder.create()
                .url(url0) // основная БД
                .username(login)
                .password(password)
                .build();
    }

    @Bean(name = "dbEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean dbEntityManagerFactory(
            @Qualifier("databasesDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "update");
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        return builder
                .dataSource(dataSource)
                .packages("com.nurtel.vaskamailio.db.entity")
                .persistenceUnit("databases")
//                .properties(jpaProperties)
                .build();
    }

    @Bean(name = "databasesTransactionManager")
    public PlatformTransactionManager databasesTransactionManager(
            @Qualifier("dbEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
        return new EntityManagerFactoryBuilder(
                new HibernateJpaVendorAdapter(), new HashMap<>(), null);
    }
}
