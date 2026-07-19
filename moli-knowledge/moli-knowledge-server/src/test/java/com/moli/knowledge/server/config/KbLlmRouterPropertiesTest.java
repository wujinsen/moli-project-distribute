package com.moli.knowledge.server.config;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class KbLlmRouterPropertiesTest {

    @Test
    public void normalizedFallbacks_skipsIncompleteAndCapsAtThree() {
        KbLlmRouterProperties props = new KbLlmRouterProperties();

        KbLlmRouterProperties.Fallback ok = entry("deepseek", "https://api.deepseek.com/v1",
                "KB_LLM_FALLBACK_1_KEY", "deepseek-chat");
        KbLlmRouterProperties.Fallback incomplete = new KbLlmRouterProperties.Fallback();
        incomplete.setProvider("qwen");

        props.setFallbacks(Arrays.asList(ok, incomplete, entry("a", "https://a/v1", "K1", "m1"),
                entry("b", "https://b/v1", "K2", "m2"), entry("c", "https://c/v1", "K3", "m3")));

        Assert.assertEquals(3, props.normalizedFallbacks().size());
        Assert.assertEquals("deepseek", props.normalizedFallbacks().get(0).getProvider());
    }

    @Test
    public void normalizedFallbacks_emptyWhenNull() {
        KbLlmRouterProperties props = new KbLlmRouterProperties();
        props.setFallbacks(null);
        Assert.assertEquals(Collections.emptyList(), props.normalizedFallbacks());
    }

    private static KbLlmRouterProperties.Fallback entry(String provider, String baseUrl, String env, String model) {
        KbLlmRouterProperties.Fallback fb = new KbLlmRouterProperties.Fallback();
        fb.setProvider(provider);
        fb.setBaseUrl(baseUrl);
        fb.setApiKeyEnv(env);
        fb.setModel(model);
        return fb;
    }
}
