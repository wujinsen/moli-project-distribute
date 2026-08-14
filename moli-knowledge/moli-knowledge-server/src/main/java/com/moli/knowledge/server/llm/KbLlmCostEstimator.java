package com.moli.knowledge.server.llm;

import com.moli.knowledge.server.config.KbLlmRouterProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;

/**
 * AI-8 §1.5 粗算 tokens / 成本（非计费权威）。
 */
@Component
public class KbLlmCostEstimator {

    @Resource
    private KbLlmRouterProperties routerProperties;

    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 1;
        }
        int len = text.getBytes(StandardCharsets.UTF_8).length;
        return Math.max(1, (int) Math.ceil(len / 4.0));
    }

    public int estimatePromptTokens(String systemPrompt, String userPrompt) {
        return estimateTokens(systemPrompt) + estimateTokens(userPrompt);
    }

    public int estimateCompletionTokens(String answer) {
        return estimateTokens(answer);
    }

    public BigDecimal estimateCostUsd(int promptTokensEst, int completionTokensEst) {
        KbLlmRouterProperties.Pricing.Rate rate = routerProperties.getPricing().getDefaultRate();
        double inputPer1k = rate.getInputPer1kUsd();
        double outputPer1k = rate.getOutputPer1kUsd();
        double cost = (promptTokensEst * inputPer1k + completionTokensEst * outputPer1k) / 1000.0;
        return BigDecimal.valueOf(cost).setScale(6, RoundingMode.HALF_UP);
    }

    public BigDecimal estimateCallCostUsd(String systemPrompt, String userPrompt, String answer) {
        return estimateCostUsd(
                estimatePromptTokens(systemPrompt, userPrompt),
                estimateCompletionTokens(answer));
    }
}
