package com.moli.knowledge.server.util;

import org.junit.Assert;
import org.junit.Test;

public class IngestLlmGenerateModeUtilTest {

    @Test
    public void resolve_templateModeRequested() {
        IngestLlmGenerateModeUtil.Result r = IngestLlmGenerateModeUtil.resolve(false, false);
        Assert.assertFalse(r.isEffectiveUseLlm());
        Assert.assertTrue(r.isTemplateMode());
        Assert.assertFalse(r.isLlmFallback());
        Assert.assertNull(r.getLlmFallbackReason());
    }

    @Test
    public void resolve_llmUsable() {
        IngestLlmGenerateModeUtil.Result r = IngestLlmGenerateModeUtil.resolve(true, true);
        Assert.assertTrue(r.isEffectiveUseLlm());
        Assert.assertFalse(r.isTemplateMode());
        Assert.assertFalse(r.isLlmFallback());
    }

    @Test
    public void resolve_llmUnusable_fallbackToTemplate() {
        IngestLlmGenerateModeUtil.Result r = IngestLlmGenerateModeUtil.resolve(true, false);
        Assert.assertFalse(r.isEffectiveUseLlm());
        Assert.assertTrue(r.isTemplateMode());
        Assert.assertTrue(r.isLlmFallback());
        Assert.assertEquals(IngestLlmGenerateModeUtil.FALLBACK_REASON, r.getLlmFallbackReason());
    }
}
