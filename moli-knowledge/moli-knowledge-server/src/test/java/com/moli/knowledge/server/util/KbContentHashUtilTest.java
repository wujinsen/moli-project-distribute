package com.moli.knowledge.server.util;

import org.junit.Assert;
import org.junit.Test;

public class KbContentHashUtilTest {

    @Test
    public void sha256_isDeterministic() {
        String text = "---\ntitle: Demo\n---\nbody";
        Assert.assertEquals(KbContentHashUtil.sha256(text), KbContentHashUtil.sha256(text));
    }

    @Test
    public void sha256_emptyString_hasKnownLength() {
        String hash = KbContentHashUtil.sha256("");
        Assert.assertEquals(64, hash.length());
        Assert.assertEquals(
                "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                hash);
    }

    @Test
    public void sha256_nullTreatedAsEmpty() {
        Assert.assertEquals(KbContentHashUtil.sha256(null), KbContentHashUtil.sha256(""));
    }
}
