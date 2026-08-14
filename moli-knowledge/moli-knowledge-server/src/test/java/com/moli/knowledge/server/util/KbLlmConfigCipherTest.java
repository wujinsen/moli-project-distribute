package com.moli.knowledge.server.util;

import org.junit.Assert;
import org.junit.Test;

public class KbLlmConfigCipherTest {

    private static final String SECRET = "test-kb-llm-config-secret-for-unit-test";

    @Test
    public void encryptDecrypt_roundTrip() {
        String plain = "sk-test-api-key-1234567890";
        String cipher = KbLlmConfigCipher.encrypt(plain, SECRET);
        Assert.assertNotNull(cipher);
        Assert.assertNotEquals(plain, cipher);
        Assert.assertEquals(plain, KbLlmConfigCipher.decrypt(cipher, SECRET));
    }

    @Test
    public void maskApiKey_showsLastFour() {
        Assert.assertEquals("****7890", KbLlmConfigCipher.maskApiKey("sk-test-api-key-1234567890"));
        Assert.assertNull(KbLlmConfigCipher.maskApiKey(""));
    }

    @Test
    public void resolveKey_supportsSha256Fallback() {
        byte[] key = KbLlmConfigCipher.resolveKey(SECRET);
        Assert.assertNotNull(key);
        Assert.assertEquals(32, key.length);
    }
}
