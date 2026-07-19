package com.moli.knowledge.server.llm;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * AI-8 §1.3 精确缓存键。
 */
public final class KbLlmCacheKeyBuilder {

    private static final String PREFIX = "kb:llm:cache:v1:";

    private KbLlmCacheKeyBuilder() {
    }

    public static String buildExactKey(String userPrompt, String scene, String model, String systemPrompt) {
        String normScene = StringUtils.isBlank(scene) ? "default" : scene.trim();
        String normUser = KbLlmPromptNormalizer.normalize(userPrompt);
        String contextFingerprint = sha256Hex(KbLlmPromptNormalizer.normalize(systemPrompt)).substring(0, 16);
        String payload = normUser + "\n" + normScene + "\n" + model + "\n" + contextFingerprint;
        return PREFIX + sha256Hex(payload);
    }

    public static String contextFingerprint(String systemPrompt) {
        return sha256Hex(KbLlmPromptNormalizer.normalize(systemPrompt)).substring(0, 16);
    }

    public static String vecIndexKey(String scene, String model) {
        String normScene = StringUtils.isBlank(scene) ? "default" : scene.trim();
        return "kb:llm:cache:vec:v1:" + normScene + ":" + model;
    }

    static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((input == null ? "" : input).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
