package com.moli.ai.server.bi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * §3.3 bi.agent.* 契约常量。
 */
@Data
@ConfigurationProperties(prefix = "bi.agent")
public class BiAgentProperties {

    private String baseUrl = "http://127.0.0.1:1130";

    private int timeoutMs = 60000;

    public boolean configured() {
        return baseUrl != null && !baseUrl.trim().isEmpty();
    }

    public int normalizedTimeoutMs() {
        return timeoutMs <= 0 ? 60000 : timeoutMs;
    }
}
