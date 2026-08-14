package com.moli.knowledge.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI-8 语义缓存（§2.2 kb.llm.cache.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "kb.llm.cache")
public class KbLlmCacheProperties {

    private boolean enabled = false;
    private int ttlSeconds = 3600;
    private boolean approxEnabled = false;
    private double similarityThreshold = 0.92;
    private int approxMaxEntries = 500;
    private int embedTimeoutMs = 800;
}
