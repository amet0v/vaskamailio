package com.nurtel.vaskamailio.db;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {
    @Value("${spring.datasource.url1}")
    private String url1;
    @Value("${spring.datasource.url2}")
    private String url2;
    @Value("${spring.datasource.url3}")
    private String url3;
    @Value("${spring.datasource.username}")
    private String login;
    @Value("${spring.datasource.password}")
    private String password;

    @Bean(name = "ds1")
    public DataSource dataSource1() {
        return DataSourceBuilder.create()
                .url(url1)
                .username(login)
                .password(password)
                .build();
    }

    @Bean(name = "ds2")
    public DataSource dataSource2() {
        return DataSourceBuilder.create()
                .url(url2)
                .username(login)
                .password(password)
                .build();
    }

    @Bean(name = "ds3")
    public DataSource dataSource3() {
        return DataSourceBuilder.create()
                .url(url3)
                .username(login)
                .password(password)
                .build();
    }

    @Primary
    @Bean
    public DataSource dataSource(DataSource ds1, DataSource ds2, DataSource ds3) {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("kamailio01", ds1);
        targetDataSources.put("kamailio02", ds2);
        targetDataSources.put("kamailio03", ds3);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(ds1);
        return routingDataSource;
    }
}
