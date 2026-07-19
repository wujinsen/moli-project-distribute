package com.moli.knowledge.server.service;

import com.moli.knowledge.server.config.KbLlmCacheProperties;
import com.moli.knowledge.server.config.KbLlmRouterProperties;
import com.moli.knowledge.server.llm.KbLlmCostEstimator;
import com.moli.knowledge.server.llm.KbLlmEffectiveConfig;
import com.moli.knowledge.server.llm.KbLlmProviderAdapter;
import com.moli.knowledge.server.llm.KbLlmRouter;
import com.moli.knowledge.server.llm.KbLlmRuntime;
import com.moli.knowledge.server.llm.KbLlmSemanticCache;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbLlmClientRouterTest {

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

    private KbLlmEffectiveConfig primary;

    @Before
    public void setUp() {
        primary = KbLlmEffectiveConfig.builder()
                .enabled(true)
                .provider("glm")
                .baseUrl("https://open.bigmodel.cn/api/paas/v4")
                .apiKey("key")
                .model("glm-4-flash")
                .temperature(0.3)
                .timeoutSeconds(90)
                .build();
        when(llm.usable()).thenReturn(true);
        when(llm.getProvider()).thenReturn("glm");
        when(llm.getModel()).thenReturn("glm-4-flash");
        when(llm.current()).thenReturn(primary);
        when(cacheProperties.isEnabled()).thenReturn(false);
    }

    @Test
    public void chat_routerDisabled_usesLegacyPath() throws Exception {
        when(routerProperties.isEnabled()).thenReturn(false);
        when(providerAdapter.chatLegacy(eq(primary), eq("sys"), eq("user"), isNull()))
                .thenReturn("legacy-answer");
        when(costEstimator.estimatePromptTokens(any(), any())).thenReturn(8);
        when(costEstimator.estimateCompletionTokens(any())).thenReturn(4);
        when(costEstimator.estimateCostUsd(anyInt(), anyInt()))
                .thenReturn(new BigDecimal("0.000012"));

        String answer = kbLlmClient.chat("ask", 1L, "sys", "user", null);

        Assert.assertEquals("legacy-answer", answer);
        verify(kbLlmRouter, never()).execute(any(), any(), any(), any());
        verify(providerAdapter).chatLegacy(eq(primary), eq("sys"), eq("user"), isNull());
        verify(callLogService).recordSuccess(eq("ask"), eq(1L), eq("glm"), eq("glm-4-flash"), anyLong(),
                eq(false), eq(false), eq(8), eq(4), any(BigDecimal.class));
    }
}
