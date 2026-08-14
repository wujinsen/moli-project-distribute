package com.moli.ai.server.bi.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * ChatBI 只读查询数据源（INV-1 · 与 spring.datasource 写库隔离）。
 */
@Configuration
@EnableConfigurationProperties(BiChatProperties.class)
public class BiChatReadonlyDataSourceConfig {

    public static final String READONLY_DATASOURCE = "biChatReadonlyDataSource";
    public static final String READONLY_JDBC_TEMPLATE = "biChatReadonlyJdbcTemplate";

    @Bean(name = READONLY_DATASOURCE)
    @ConditionalOnProperty(prefix = "bi.chat.readonly-datasource", name = "url")
    public DataSource biChatReadonlyDataSource(BiChatProperties properties) {
        BiChatProperties.ReadonlyDatasource cfg = properties.getReadonlyDatasource();
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(cfg.getUrl());
        ds.setUsername(cfg.getUsername());
        ds.setPassword(cfg.getPassword());
        ds.setDriverClassName(cfg.getDriverClassName());
        ds.setReadOnly(true);
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(1);
        ds.setPoolName("bi-chat-readonly");
        return ds;
    }

    @Bean(name = READONLY_JDBC_TEMPLATE)
    @ConditionalOnProperty(prefix = "bi.chat.readonly-datasource", name = "url")
    public JdbcTemplate biChatReadonlyJdbcTemplate(
            @org.springframework.beans.factory.annotation.Qualifier(READONLY_DATASOURCE) DataSource dataSource) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.setQueryTimeout(30);
        return jdbc;
    }
}
