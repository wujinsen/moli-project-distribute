package com.moli.knowledge.server.llm;

import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbLlmRouterProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * AI-8 failover 路由：primary（KbLlmRuntime）→ 有序 fallback 链（R-INV-1~3）。
 */
@Slf4j
@Component
public class KbLlmRouter {

    @Resource
    private KbLlmRouterProperties routerProperties;
    @Resource
    private KbLlmProviderAdapter providerAdapter;

    /**
     * router.enabled=false 或 fallbacks 为空时，仅调 primary（与现网一致，含 retry）。
     */
    public KbLlmRouterResult execute(KbLlmEffectiveConfig primary, String systemPrompt, String userPrompt,
                                     String modelOverride) throws Exception {
        String model = resolveModel(modelOverride, primary.getModel());
        if (!routerProperties.isEnabled() || routerProperties.normalizedFallbacks().isEmpty()) {
            String answer = invokeWithRetry(primary, systemPrompt, userPrompt, modelOverride, primary.getProvider());
            return KbLlmRouterResult.builder()
                    .answer(answer)
                    .provider(primary.getProvider())
                    .model(model)
                    .failover(false)
                    .build();
        }

        LlmRetryableException lastRetryable = null;
        try {
            String answer = invokeWithRetry(primary, systemPrompt, userPrompt, modelOverride, primary.getProvider());
            return KbLlmRouterResult.builder()
                    .answer(answer)
                    .provider(primary.getProvider())
                    .model(model)
                    .failover(false)
                    .build();
        } catch (BaseException e) {
            throw e;
        } catch (LlmRetryableException e) {
            lastRetryable = e;
            log.warn("[kb-llm-router] primary failed, trying fallbacks: {}", e.getMessage());
        }

        List<KbLlmRouterProperties.Fallback> fallbacks = routerProperties.normalizedFallbacks();
        for (KbLlmRouterProperties.Fallback fb : fallbacks) {
            KbLlmEffectiveConfig cfg = providerAdapter.resolveFallbackConfig(fb, primary);
            if (cfg == null || !cfg.usable()) {
                log.warn("[kb-llm-router] skip fallback {} (missing env {})", fb.getProvider(), fb.getApiKeyEnv());
                continue;
            }
            try {
                String answer = providerAdapter.chat(cfg, systemPrompt, userPrompt, modelOverride);
                return KbLlmRouterResult.builder()
                        .answer(answer)
                        .provider(cfg.getProvider())
                        .model(resolveModel(modelOverride, cfg.getModel()))
                        .failover(true)
                        .build();
            } catch (BaseException e) {
                throw e;
            } catch (LlmRetryableException e) {
                lastRetryable = e;
                log.warn("[kb-llm-router] fallback {} failed: {}", cfg.getProvider(), e.getMessage());
            }
        }

        if (lastRetryable != null) {
            throw new BaseException("LLM 调用失败：" + lastRetryable.getMessage());
        }
        throw new BaseException("LLM 调用失败：所有 provider 均不可用");
    }

    private String invokeWithRetry(KbLlmEffectiveConfig cfg, String systemPrompt, String userPrompt,
                                   String modelOverride, String providerLabel) throws Exception {
        int maxAttempts = Math.max(0, routerProperties.getRetry()) + 1;
        LlmRetryableException last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return providerAdapter.chat(cfg, systemPrompt, userPrompt, modelOverride);
            } catch (BaseException e) {
                throw e;
            } catch (LlmRetryableException e) {
                last = e;
                if (attempt < maxAttempts) {
                    sleepBackoff();
                    log.warn("[kb-llm-router] retry {}/{} for {} after {}", attempt, maxAttempts - 1,
                            providerLabel, e.getMessage());
                }
            }
        }
        if (last != null) {
            throw last;
        }
        throw new BaseException("LLM 调用失败");
    }

    private void sleepBackoff() {
        long ms = routerProperties.getRetryBackoffMs();
        if (ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String resolveModel(String modelOverride, String defaultModel) {
        return StringUtils.isNotBlank(modelOverride) ? modelOverride.trim() : defaultModel;
    }
}
