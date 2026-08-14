package com.moli.knowledge.server.llm;

import org.junit.Assert;
import org.junit.Test;

public class KbLlmCacheKeyBuilderTest {

    @Test
    public void buildExactKey_sameInputsSameKey() {
        String k1 = KbLlmCacheKeyBuilder.buildExactKey("Hello", "ask", "glm-4", "You are helpful");
        String k2 = KbLlmCacheKeyBuilder.buildExactKey("hello", "ask", "glm-4", "you are helpful");
        Assert.assertEquals(k1, k2);
    }

    @Test
    public void buildExactKey_differentSceneDifferentKey() {
        String k1 = KbLlmCacheKeyBuilder.buildExactKey("q", "ask", "m", "sys");
        String k2 = KbLlmCacheKeyBuilder.buildExactKey("q", "ingest", "m", "sys");
        Assert.assertNotEquals(k1, k2);
    }

    @Test
    public void buildExactKey_differentSystemDifferentKey() {
        String k1 = KbLlmCacheKeyBuilder.buildExactKey("q", "ask", "m", "sys-a");
        String k2 = KbLlmCacheKeyBuilder.buildExactKey("q", "ask", "m", "sys-b");
        Assert.assertNotEquals(k1, k2);
    }
}
