package com.moli.knowledge.server.service;

import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbLlmCacheProperties;
import com.moli.knowledge.server.config.KbLlmRouterProperties;
import com.moli.knowledge.server.llm.KbLlmCostEstimator;
import com.moli.knowledge.server.llm.KbLlmEffectiveConfig;
import com.moli.knowledge.server.llm.KbLlmProviderAdapter;
import com.moli.knowledge.server.llm.KbLlmRouter;
import com.moli.knowledge.server.llm.KbLlmRouterResult;
import com.moli.knowledge.server.llm.KbLlmRuntime;
import com.moli.knowledge.server.llm.KbLlmSemanticCache;
import com.moli.knowledge.server.llm.KbLlmSemanticCacheEntry;
import com.moli.knowledge.server.support.KbLlmCallScenes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Optional;

/**
 * OpenAI 兼容 LLM HTTP 客户端（Ask / Wiki AI 改稿 / Ingest 共用）。
 * AI-8：语义缓存 → 路由 failover（默认关，零回归）。
 */
@Slf4j
@Service
public class KbLlmClient {

    private static final String DEFAULT_TEST_SYSTEM = "You are a helpful assistant. Reply briefly.";

    @Resource
    private KbLlmRuntime llm;
    @Resource
    private KbLlmCallLogService callLogService;
    @Resource
    private KbLlmRouter kbLlmRouter;
    @Resource
    private KbLlmRouterProperties routerProperties;
    @Resource
    private KbLlmCacheProperties cacheProperties;
    @Resource
    private KbLlmSemanticCache semanticCache;
    @Resource
    private KbLlmCostEstimator costEstimator;
    @Resource
    private KbLlmProviderAdapter providerAdapter;

    public boolean usable() {
        return llm.usable();
    }

    public String getProvider() {
        return llm.getProvider();
    }

    public String getModel() {
        return llm.getModel();
    }

    public void assertUsable() {
        if (!usable()) {
            throw new BaseException("LLM 未配置或已禁用（平台 LLM 设置或 kb.llm.enabled/api-key）");
        }
    }

    public String chat(String systemPrompt, String userPrompt) {
        return chat(null, null, systemPrompt, userPrompt, null);
    }

    public String chat(String systemPrompt, String userPrompt, String modelOverride) {
        return chat(null, null, systemPrompt, userPrompt, modelOverride);
    }

    public String chat(String scene, Long spaceId, String systemPrompt, String userPrompt) {
        return chat(scene, spaceId, systemPrompt, userPrompt, null);
    }

    /** 带 scene/space 的调用（写入 kb_llm_call_log）。 */
    public String chat(String scene, Long spaceId, String systemPrompt, String userPrompt, String modelOverride) {
        assertUsable();
        long start = System.currentTimeMillis();
        String provider = getProvider();
        String model = resolveModel(modelOverride);

        if (cacheProperties.isEnabled()) {
            Optional<KbLlmSemanticCacheEntry> cached =
                    semanticCache.lookup(scene, model, systemPrompt, userPrompt);
            if (cached.isPresent()) {
                KbLlmSemanticCacheEntry entry = cached.get();
                int promptTokens = costEstimator.estimatePromptTokens(systemPrompt, userPrompt);
                int completionTokens = costEstimator.estimateCompletionTokens(entry.getAnswer());
                callLogService.recordSuccess(scene, spaceId, entry.getProvider(), entry.getModel(), elapsed(start),
                        false, true, promptTokens, completionTokens, BigDecimal.ZERO);
                return entry.getAnswer();
            }
        }

        try {
            String answer;
            String actualProvider;
            String actualModel;
            boolean failover = false;

            if (!routerProperties.isEnabled()) {
                answer = providerAdapter.chatLegacy(llm.current(), systemPrompt, userPrompt, modelOverride);
                actualProvider = provider;
                actualModel = model;
            } else {
                KbLlmRouterResult routed = kbLlmRouter.execute(llm.current(), systemPrompt, userPrompt, modelOverride);
                answer = routed.getAnswer();
                actualProvider = routed.getProvider();
                actualModel = routed.getModel();
                failover = routed.isFailover();
            }

            if (cacheProperties.isEnabled()) {
                semanticCache.put(scene, model, systemPrompt, userPrompt, answer, actualProvider, actualModel);
            }

            int promptTokens = costEstimator.estimatePromptTokens(systemPrompt, userPrompt);
            int completionTokens = costEstimator.estimateCompletionTokens(answer);
            BigDecimal costUsd = costEstimator.estimateCostUsd(promptTokens, completionTokens);
            callLogService.recordSuccess(scene, spaceId, actualProvider, actualModel, elapsed(start),
                    failover, false, promptTokens, completionTokens, costUsd);
            return answer;
        } catch (BaseException e) {
            callLogService.recordFail(scene, spaceId, provider, model, elapsed(start), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.warn("LLM 调用失败: {}", e.getMessage());
            callLogService.recordFail(scene, spaceId, provider, model, elapsed(start), e.getMessage());
            throw new BaseException("LLM 调用失败：" + e.getMessage());
        }
    }

    /** 使用指定配置调用（连通性测试等），不经 Router / 语义缓存。 */
    public String chatWithConfig(KbLlmEffectiveConfig cfg, String systemPrompt, String userPrompt, String modelOverride)
            throws Exception {
        return chatWithConfig(cfg, KbLlmCallScenes.LLM_TEST, null, systemPrompt, userPrompt, modelOverride);
    }

    public String chatWithConfig(KbLlmEffectiveConfig cfg, String scene, Long spaceId,
                                 String systemPrompt, String userPrompt, String modelOverride) throws Exception {
        if (cfg == null || !cfg.usable()) {
            throw new BaseException("LLM 未配置或已禁用");
        }
        long start = System.currentTimeMillis();
        String provider = cfg.getProvider();
        String model = resolveModel(modelOverride, cfg.getModel());
        try {
            String result = providerAdapter.chatLegacy(cfg, systemPrompt, userPrompt, modelOverride);
            callLogService.recordSuccess(scene, spaceId, provider, model, elapsed(start));
            return result;
        } catch (BaseException e) {
            callLogService.recordFail(scene, spaceId, provider, model, elapsed(start), e.getMessage());
            throw e;
        } catch (Exception e) {
            callLogService.recordFail(scene, spaceId, provider, model, elapsed(start), e.getMessage());
            throw e;
        }
    }

    public String testPing(KbLlmEffectiveConfig cfg, String userMessage) throws Exception {
        String msg = userMessage == null || userMessage.trim().isEmpty() ? "ping" : userMessage.trim();
        return chatWithConfig(cfg, KbLlmCallScenes.LLM_TEST, null, DEFAULT_TEST_SYSTEM, msg, null);
    }

    private static long elapsed(long start) {
        return System.currentTimeMillis() - start;
    }

    private String resolveModel(String modelOverride) {
        return resolveModel(modelOverride, getModel());
    }

    private static String resolveModel(String modelOverride, String defaultModel) {
        return StringUtils.isNotBlank(modelOverride) ? modelOverride.trim() : defaultModel;
    }
}
