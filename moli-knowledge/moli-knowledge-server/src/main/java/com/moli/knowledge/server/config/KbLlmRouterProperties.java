package com.moli.knowledge.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AI-8 LLM 路由 / failover（§2.1 kb.llm.router.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "kb.llm.router")
public class KbLlmRouterProperties {

    private boolean enabled = false;
    private String mode = "failover";
    private int retry = 0;
    private long retryBackoffMs = 200L;
    private List<Fallback> fallbacks = new ArrayList<>();
    private Pricing pricing = new Pricing();

    public List<Fallback> normalizedFallbacks() {
        if (fallbacks == null || fallbacks.isEmpty()) {
            return Collections.emptyList();
        }
        List<Fallback> out = new ArrayList<>();
        for (Fallback fb : fallbacks) {
            if (fb != null && fb.isConfigured()) {
                out.add(fb);
            }
        }
        return out.size() > 3 ? out.subList(0, 3) : out;
    }

    @Data
    public static class Fallback {
        private String provider;
        private String baseUrl;
        /** env 变量名，如 KB_LLM_FALLBACK_1_KEY */
        private String apiKeyEnv;
        private String model;
        private Integer timeoutSeconds;

        boolean isConfigured() {
            return provider != null && !provider.trim().isEmpty()
                    && baseUrl != null && !baseUrl.trim().isEmpty()
                    && apiKeyEnv != null && !apiKeyEnv.trim().isEmpty()
                    && model != null && !model.trim().isEmpty();
        }
    }

    @Data
    public static class Pricing {
        private Rate defaultRate = new Rate();

        @Data
        public static class Rate {
            private double inputPer1kUsd = 0.001;
            private double outputPer1kUsd = 0.002;
        }
    }
}
