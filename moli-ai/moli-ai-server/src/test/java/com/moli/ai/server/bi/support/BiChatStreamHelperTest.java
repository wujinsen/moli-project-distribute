package com.moli.ai.server.bi.support;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class BiChatStreamHelperTest {

    @Test
    public void splitExplanationTokens_chunksChineseText() {
        List<String> tokens = BiChatStreamHelper.splitExplanationTokens("秒杀订单共100条");
        Assert.assertFalse(tokens.isEmpty());
        String joined = String.join("", tokens);
        Assert.assertEquals("秒杀订单共100条", joined);
    }

    @Test
    public void splitExplanationTokens_emptyReturnsEmpty() {
        Assert.assertTrue(BiChatStreamHelper.splitExplanationTokens(null).isEmpty());
        Assert.assertTrue(BiChatStreamHelper.splitExplanationTokens("").isEmpty());
    }
}
