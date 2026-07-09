package com.moli.user.center.server.operation.util;

import org.junit.Assert;
import org.junit.Test;

public class OperationSecretCipherTest {

    private static final String SECRET = "test-ops-secret-key-for-unit-test";

    @Test
    public void encryptDecrypt_roundTrip() {
        String plain = "change-me-prod-password";
        String cipher = OperationSecretCipher.encrypt(plain, SECRET);
        Assert.assertNotNull(cipher);
        Assert.assertNotEquals(plain, cipher);
        Assert.assertEquals(plain, OperationSecretCipher.decrypt(cipher, SECRET));
    }

    @Test
    public void maskSecret_showsLastFour() {
        Assert.assertEquals("****word", OperationSecretCipher.maskSecret("change-me-prod-password"));
        Assert.assertNull(OperationSecretCipher.maskSecret(""));
    }

    @Test
    public void looksLikeCipherText_detectsEncryptedPayload() {
        String cipher = OperationSecretCipher.encrypt("secret", SECRET);
        Assert.assertTrue(OperationSecretCipher.looksLikeCipherText(cipher));
        Assert.assertFalse(OperationSecretCipher.looksLikeCipherText("plain-text"));
    }
}
