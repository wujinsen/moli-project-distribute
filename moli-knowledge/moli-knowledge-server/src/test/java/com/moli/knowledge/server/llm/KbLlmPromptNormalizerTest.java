package com.moli.knowledge.server.llm;

import org.junit.Assert;
import org.junit.Test;

public class KbLlmPromptNormalizerTest {

    @Test
    public void normalize_trimsAndCollapsesWhitespace() {
        Assert.assertEquals("hello world", KbLlmPromptNormalizer.normalize("  hello   world  "));
    }

    @Test
    public void normalize_asciiLowerOnly() {
        Assert.assertEquals("abc 中文", KbLlmPromptNormalizer.normalize("ABC 中文"));
    }
}
