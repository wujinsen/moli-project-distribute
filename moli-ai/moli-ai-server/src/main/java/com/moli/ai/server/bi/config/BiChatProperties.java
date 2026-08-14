package com.moli.ai.server.bi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * §3.3 契约常量（键名冻结）。
 */
@Data
@ConfigurationProperties(prefix = "bi.chat")
public class BiChatProperties {

    /** 表白名单；空/未配置 = deny-all（INV-5） */
    private List<String> allowTables = new ArrayList<>();

    /** 列黑名单（INV-6） */
    private List<String> denyColumns = defaultDenyColumns();

    private int defaultRows = 100;

    private int maxRows = 1000;

    private int maxScanRows = 10000;

    private long queryTimeoutMs = 30000L;

    private int maxRetry = 2;

    /** SSE 连接超时（毫秒） */
    private long sseTimeoutMs = 120000L;

    private ReadonlyDatasource readonlyDatasource = new ReadonlyDatasource();

    @Data
    public static class ReadonlyDatasource {
        private String url;
        private String username;
        private String password;
        private String driverClassName = "com.mysql.cj.jdbc.Driver";
    }

    private static List<String> defaultDenyColumns() {
        List<String> cols = new ArrayList<>();
        cols.add("password");
        cols.add("salt");
        cols.add("token");
        cols.add("secret");
        return cols;
    }
}
