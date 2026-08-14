package com.moli.knowledge.server.util;

import com.moli.common.exception.BaseException;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 平台 LLM api-key 加解密（AES-256-GCM）。
 * <p>
 * 密钥：环境变量 {@code KB_LLM_CONFIG_SECRET} 或 {@code kb.llm.config-secret}（32 字节 Base64 或其 UTF-8 字符串 SHA-256）。
 */
public final class KbLlmConfigCipher {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private KbLlmConfigCipher() {
    }

    public static byte[] resolveKey(String secret) {
        if (StringUtils.isBlank(secret)) {
            return null;
        }
        String trimmed = secret.trim();
        try {
            byte[] decoded = Base64.getDecoder().decode(trimmed);
            if (decoded.length == 32) {
                return decoded;
            }
        } catch (IllegalArgumentException ignored) {
            // fall through to SHA-256
        }
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(trimmed.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new BaseException("无法解析 KB_LLM_CONFIG_SECRET: " + e.getMessage());
        }
    }

    public static String encrypt(String plainText, String secret) {
        if (StringUtils.isBlank(plainText)) {
            return null;
        }
        byte[] key = resolveKey(secret);
        if (key == null) {
            throw new BaseException("未配置 kb.llm.config-secret，无法加密 api-key");
        }
        try {
            byte[] iv = new byte[IV_BYTES];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(payload);
        } catch (Exception e) {
            throw new BaseException("api-key 加密失败: " + e.getMessage());
        }
    }

    public static String decrypt(String cipherText, String secret) {
        if (StringUtils.isBlank(cipherText)) {
            return null;
        }
        byte[] key = resolveKey(secret);
        if (key == null) {
            throw new BaseException("未配置 kb.llm.config-secret，无法解密 api-key");
        }
        try {
            byte[] payload = Base64.getDecoder().decode(cipherText.trim());
            if (payload.length <= IV_BYTES) {
                throw new BaseException("api-key 密文格式非法");
            }
            byte[] iv = new byte[IV_BYTES];
            System.arraycopy(payload, 0, iv, 0, IV_BYTES);
            byte[] encrypted = new byte[payload.length - IV_BYTES];
            System.arraycopy(payload, IV_BYTES, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
            byte[] plain = cipher.doFinal(encrypted);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException("api-key 解密失败: " + e.getMessage());
        }
    }

    public static String maskApiKey(String apiKey) {
        if (StringUtils.isBlank(apiKey)) {
            return null;
        }
        String key = apiKey.trim();
        if (key.length() <= 4) {
            return "****";
        }
        return "****" + key.substring(key.length() - 4);
    }
}
