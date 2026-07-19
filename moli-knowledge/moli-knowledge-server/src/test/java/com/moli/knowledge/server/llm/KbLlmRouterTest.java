package com.moli.knowledge.server.llm;

import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbLlmRouterProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbLlmRouterTest {

    @InjectMocks
    private KbLlmRouter router;

    @Mock
    private KbLlmRouterProperties routerProperties;

    @Mock
    private KbLlmProviderAdapter providerAdapter;

    private KbLlmEffectiveConfig primary;

    @Before
    public void setUp() {
        primary = KbLlmEffectiveConfig.builder()
                .enabled(true)
                .provider("glm")
                .baseUrl("https://open.bigmodel.cn/api/paas/v4")
                .apiKey("primary-key")
                .model("glm-4-flash")
                .temperature(0.3)
                .timeoutSeconds(90)
                .source(KbLlmConfigSource.DATABASE)
                .build();
    }

    @Test
    public void execute_routerDisabled_primarySuccess() throws Exception {
        when(routerProperties.isEnabled()).thenReturn(false);
        when(providerAdapter.chat(eq(primary), eq("sys"), eq("user"), isNull()))
                .thenReturn("primary-answer");

        KbLlmRouterResult result = router.execute(primary, "sys", "user", null);

        Assert.assertEquals("primary-answer", result.getAnswer());
        Assert.assertEquals("glm", result.getProvider());
        Assert.assertEquals("glm-4-flash", result.getModel());
        Assert.assertFalse(result.isFailover());
        verify(providerAdapter, times(1)).chat(eq(primary), eq("sys"), eq("user"), isNull());
    }

    @Test
    public void execute_primaryRetryableFail_fallbackSuccess() throws Exception {
        when(routerProperties.isEnabled()).thenReturn(true);
        when(routerProperties.getRetry()).thenReturn(0);
        when(routerProperties.normalizedFallbacks()).thenReturn(Collections.singletonList(fallbackEntry()));

        when(providerAdapter.chat(eq(primary), any(), any(), any()))
                .thenThrow(new LlmRetryableException("HTTP 503: unavailable", 503));

        KbLlmEffectiveConfig fbCfg = fallbackConfig();
        when(providerAdapter.resolveFallbackConfig(any(), eq(primary))).thenReturn(fbCfg);
        when(providerAdapter.chat(eq(fbCfg), eq("sys"), eq("user"), isNull()))
                .thenReturn("fallback-answer");

        KbLlmRouterResult result = router.execute(primary, "sys", "user", null);

        Assert.assertEquals("fallback-answer", result.getAnswer());
        Assert.assertEquals("deepseek", result.getProvider());
        Assert.assertEquals("deepseek-chat", result.getModel());
        Assert.assertTrue(result.isFailover());
    }

    @Test
    public void execute_primary4xx_noFallback() throws Exception {
        when(routerProperties.isEnabled()).thenReturn(true);
        when(routerProperties.getRetry()).thenReturn(0);
        when(routerProperties.normalizedFallbacks()).thenReturn(Collections.singletonList(fallbackEntry()));

        when(providerAdapter.chat(eq(primary), any(), any(), any()))
                .thenThrow(new BaseException("HTTP 401: unauthorized"));

        try {
            router.execute(primary, "sys", "user", null);
            Assert.fail("expected BaseException");
        } catch (BaseException e) {
            Assert.assertTrue(e.getMessage().contains("401"));
        }
        verify(providerAdapter, never()).resolveFallbackConfig(any(), any());
    }

    @Test
    public void execute_allProvidersFail_throwsBaseException() throws Exception {
        when(routerProperties.isEnabled()).thenReturn(true);
        when(routerProperties.getRetry()).thenReturn(0);
        when(routerProperties.normalizedFallbacks()).thenReturn(Collections.singletonList(fallbackEntry()));

        when(providerAdapter.chat(eq(primary), any(), any(), any()))
                .thenThrow(new LlmRetryableException("HTTP 500: boom", 500));

        KbLlmEffectiveConfig fbCfg = fallbackConfig();
        when(providerAdapter.resolveFallbackConfig(any(), eq(primary))).thenReturn(fbCfg);
        when(providerAdapter.chat(eq(fbCfg), any(), any(), any()))
                .thenThrow(new LlmRetryableException("HTTP 502: bad gateway", 502));

        try {
            router.execute(primary, "sys", "user", null);
            Assert.fail("expected BaseException");
        } catch (BaseException e) {
            Assert.assertTrue(e.getMessage().startsWith("LLM 调用失败："));
            Assert.assertTrue(e.getMessage().contains("502"));
        }
    }

    @Test
    public void execute_enabledNoFallbacks_primaryOnly() throws Exception {
        when(routerProperties.isEnabled()).thenReturn(true);
        when(routerProperties.getRetry()).thenReturn(0);
        when(routerProperties.normalizedFallbacks()).thenReturn(Collections.emptyList());
        when(providerAdapter.chat(eq(primary), any(), any(), any())).thenReturn("only-primary");

        KbLlmRouterResult result = router.execute(primary, "sys", "user", null);

        Assert.assertEquals("only-primary", result.getAnswer());
        Assert.assertFalse(result.isFailover());
        verify(providerAdapter, never()).resolveFallbackConfig(any(), any());
    }

    private static KbLlmRouterProperties.Fallback fallbackEntry() {
        KbLlmRouterProperties.Fallback fb = new KbLlmRouterProperties.Fallback();
        fb.setProvider("deepseek");
        fb.setBaseUrl("https://api.deepseek.com/v1");
        fb.setApiKeyEnv("KB_LLM_FALLBACK_1_KEY");
        fb.setModel("deepseek-chat");
        fb.setTimeoutSeconds(60);
        return fb;
    }

    private static KbLlmEffectiveConfig fallbackConfig() {
        return KbLlmEffectiveConfig.builder()
                .enabled(true)
                .provider("deepseek")
                .baseUrl("https://api.deepseek.com/v1")
                .apiKey("fallback-key")
                .model("deepseek-chat")
                .temperature(0.3)
                .timeoutSeconds(60)
                .source(KbLlmConfigSource.YAML_FALLBACK)
                .build();
    }
}
