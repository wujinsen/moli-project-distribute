package com.moli.knowledge.server.service;

import com.moli.knowledge.server.config.KbLlmCacheProperties;
import com.moli.knowledge.server.config.KbLlmRouterProperties;
import com.moli.knowledge.server.llm.KbLlmCostEstimator;
import com.moli.knowledge.server.llm.KbLlmProviderAdapter;
import com.moli.knowledge.server.llm.KbLlmRouter;
import com.moli.knowledge.server.llm.KbLlmRuntime;
import com.moli.knowledge.server.llm.KbLlmSemanticCache;
import com.moli.knowledge.server.llm.KbLlmSemanticCacheEntry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbLlmClientCacheTest {

    @InjectMocks
    private KbLlmClient kbLlmClient;

    @Mock
    private KbLlmRuntime llm;
    @Mock
    private KbLlmCallLogService callLogService;
    @Mock
    private KbLlmRouter kbLlmRouter;
    @Mock
    private KbLlmRouterProperties routerProperties;
    @Mock
    private KbLlmCacheProperties cacheProperties;
    @Mock
    private KbLlmSemanticCache semanticCache;
    @Mock
    private KbLlmCostEstimator costEstimator;
    @Mock
    private KbLlmProviderAdapter providerAdapter;

    @Before
    public void setUp() {
        when(llm.usable()).thenReturn(true);
        when(llm.getProvider()).thenReturn("glm");
        when(llm.getModel()).thenReturn("glm-4-flash");
        when(llm.current()).thenReturn(com.moli.knowledge.server.llm.KbLlmEffectiveConfig.builder()
                .enabled(true)
                .provider("glm")
                .baseUrl("https://example.com")
                .apiKey("k")
                .model("glm-4-flash")
                .temperature(0.3)
                .timeoutSeconds(90)
                .build());
        when(routerProperties.isEnabled()).thenReturn(false);
        when(cacheProperties.isEnabled()).thenReturn(true);
        when(costEstimator.estimatePromptTokens(any(), any())).thenReturn(10);
        when(costEstimator.estimateCompletionTokens(any())).thenReturn(5);
    }

    @Test
    public void chat_cacheHit_skipsProviderAndLogsCacheHit() throws Exception {
        KbLlmSemanticCacheEntry cached = new KbLlmSemanticCacheEntry("cached-answer", "glm", "glm-4-flash", 1L);
        when(semanticCache.lookup(eq("ask"), eq("glm-4-flash"), eq("sys"), eq("same question")))
                .thenReturn(Optional.of(cached));

        String answer = kbLlmClient.chat("ask", 1L, "sys", "same question", null);

        Assert.assertEquals("cached-answer", answer);
        verify(providerAdapter, never()).chatLegacy(any(), any(), any(), any());
        verify(callLogService).recordSuccess(eq("ask"), eq(1L), eq("glm"), eq("glm-4-flash"), anyLong(),
                eq(false), eq(true), eq(10), eq(5), eq(BigDecimal.ZERO));
    }

    @Test
    public void chat_cacheMiss_callsProviderAndPutsCache() throws Exception {
        when(semanticCache.lookup(any(), any(), any(), any())).thenReturn(Optional.empty());
        when(providerAdapter.chatLegacy(any(), eq("sys"), eq("question"), isNull())).thenReturn("fresh");
        when(costEstimator.estimateCostUsd(anyInt(), anyInt())).thenReturn(new BigDecimal("0.000015"));

        String answer = kbLlmClient.chat("ask", 1L, "sys", "question", null);

        Assert.assertEquals("fresh", answer);
        verify(semanticCache).put(eq("ask"), eq("glm-4-flash"), eq("sys"), eq("question"),
                eq("fresh"), eq("glm"), eq("glm-4-flash"));
        verify(callLogService).recordSuccess(eq("ask"), eq(1L), eq("glm"), eq("glm-4-flash"), anyLong(),
                eq(false), eq(false), eq(10), eq(5), any(BigDecimal.class));
    }
}
