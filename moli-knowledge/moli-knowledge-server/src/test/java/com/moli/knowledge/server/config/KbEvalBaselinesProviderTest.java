package com.moli.knowledge.server.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

public class KbEvalBaselinesProviderTest {

    private KbEvalBaselinesProvider provider;

    @Before
    public void setUp() {
        provider = new KbEvalBaselinesProvider();
        provider.load();
    }

    @Test
    public void evaluateGate_hybridRerank_passAtBaseline() {
        Assert.assertEquals(Boolean.TRUE, provider.evaluateGate(
                "hybrid-rerank",
                new BigDecimal("0.8333"),
                0,
                "{\"dirty\":{\"hit_at\":{\"3\":0.90}}}"));
    }

    @Test
    public void evaluateGate_hybridRerank_failOnHit3() {
        Assert.assertEquals(Boolean.FALSE, provider.evaluateGate(
                "hybrid-rerank",
                new BigDecimal("0.70"),
                0,
                "{\"dirty\":{\"hit_at\":{\"3\":0.90}}}"));
    }

    @Test
    public void evaluateGate_nullWhenNoHit3() {
        Assert.assertNull(provider.evaluateGate("hybrid-rerank", null, 0, "{}"));
    }
}
