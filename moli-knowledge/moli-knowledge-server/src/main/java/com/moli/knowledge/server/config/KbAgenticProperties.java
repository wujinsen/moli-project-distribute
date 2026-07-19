package com.moli.knowledge.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI-7 Agentic RAG 编排开关（§4.2）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "kb.agentic")
public class KbAgenticProperties {

    private boolean enabled = false;
    private int maxRounds = 2;
    private boolean decompose = true;
    private int maxSubQuestions = 3;
    private boolean selfCheck = true;
    private double coverageThreshold = 0.8;
    private long latencyBudgetMs = 20_000L;
    /** 0 = 复用 kb.ask.llm-context-top-k */
    private int perRoundContextTopK = 0;

    public int normalizedMaxRounds() {
        int v = maxRounds <= 0 ? 2 : maxRounds;
        return Math.min(v, 3);
    }

    public int normalizedMaxSubQuestions() {
        int v = maxSubQuestions <= 0 ? 3 : maxSubQuestions;
        return Math.min(v, 5);
    }

    public int resolveContextTopK(KbAskProperties askProperties) {
        if (perRoundContextTopK > 0) {
            return perRoundContextTopK;
        }
        return askProperties.normalizedLlmContextTopK();
    }
}
