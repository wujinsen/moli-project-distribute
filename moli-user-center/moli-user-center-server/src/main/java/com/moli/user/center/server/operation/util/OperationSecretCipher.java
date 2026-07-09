package com.moli.user.center.server.operation.util;

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
 * 运维凭据加解密（AES-256-GCM）。
 * 密钥：环境变量 {@code OPS_SECRET_KEY} 或配置 {@code ops.secret.key}。
 */
public final class OperationSecretCipher {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private OperationSecretCipher() {
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
            throw new BaseException("无法解析 OPS_SECRET_KEY: " + e.getMessage());
        }
    }

    public static String encrypt(String plainText, String secret) {
        if (StringUtils.isBlank(plainText)) {
            return null;
        }
        byte[] key = resolveKey(secret);
        if (key == null) {
            throw new BaseException("未配置 ops.secret.key，无法加密运维凭据");
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
            throw new BaseException("运维凭据加密失败: " + e.getMessage());
        }
    }

    public static String decrypt(String cipherText, String secret) {
        if (StringUtils.isBlank(cipherText)) {
            return null;
        }
        byte[] key = resolveKey(secret);
        if (key == null) {
            throw new BaseException("未配置 ops.secret.key，无法解密运维凭据");
        }
        try {
            byte[] payload = Base64.getDecoder().decode(cipherText.trim());
            if (payload.length <= IV_BYTES) {
                throw new BaseException("运维凭据密文格式非法");
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
            throw new BaseException("运维凭据解密失败: " + e.getMessage());
        }
    }

    public static boolean looksLikeCipherText(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        String trimmed = value.trim();
        if (trimmed.length() < IV_BYTES + 16) {
            return false;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(trimmed);
            return payload.length > IV_BYTES;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String maskSecret(String secret) {
        if (StringUtils.isBlank(secret)) {
            return null;
        }
        String value = secret.trim();
        if (value.length() <= 4) {
            return "****";
        }
        return "****" + value.substring(value.length() - 4);
    }
}
