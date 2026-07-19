package com.moli.knowledge.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI-10 DeepResearch（§4.3 kb.research.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "kb.research")
public class KbResearchProperties {

    private boolean enabled = false;
    private String sidecarBaseUrl = "http://127.0.0.1:8095";
    private int sidecarTimeoutMs = 120_000;
    private int maxSections = 6;
    private int maxRetrieveRounds = 2;
    private long latencyBudgetMs = 90_000L;
    private int perSectionTopK = 8;
    private double coverageThreshold = 0.75;
    private boolean retrieverAgentic = false;
    private String defaultRetrievalStrategy = "hybrid";
    private boolean writebackAutoSync = true;
    private boolean guardrails = true;
    private long sseTimeoutMs = 120_000L;
    private Long writebackSpaceId = 900000000000000003L;
    private String writebackRawPath = "deep-research/writeback-stub.md";

    public int normalizedMaxSections(Integer override) {
        int v = override != null && override > 0 ? override : maxSections;
        if (v <= 0) {
            v = 6;
        }
        return Math.min(v, 10);
    }

    public int normalizedMaxRetrieveRounds(Integer override) {
        int v = override != null && override > 0 ? override : maxRetrieveRounds;
        if (v <= 0) {
            v = 2;
        }
        return Math.min(v, 3);
    }

    public long normalizedLatencyBudgetMs(Integer override) {
        if (override != null && override > 0) {
            return override;
        }
        return latencyBudgetMs > 0 ? latencyBudgetMs : 90_000L;
    }

    public int normalizedSidecarTimeoutMs() {
        return sidecarTimeoutMs > 0 ? sidecarTimeoutMs : 120_000;
    }

    public boolean configured() {
        return sidecarBaseUrl != null && !sidecarBaseUrl.trim().isEmpty();
    }
}
