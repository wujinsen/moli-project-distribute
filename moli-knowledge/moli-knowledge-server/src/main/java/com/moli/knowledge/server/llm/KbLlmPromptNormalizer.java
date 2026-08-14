package com.moli.knowledge.server.llm;

import java.text.Normalizer;

/**
 * AI-8 §1.3：trim → 连续空白压单空格 → NFKC → ASCII lower。
 */
public final class KbLlmPromptNormalizer {

    private KbLlmPromptNormalizer() {
    }

    public static String normalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String trimmed = text.trim().replaceAll("\\s+", " ");
        String nfkc = Normalizer.normalize(trimmed, Normalizer.Form.NFKC);
        return toAsciiLower(nfkc);
    }

    private static String toAsciiLower(String value) {
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c >= 'A' && c <= 'Z') {
                chars[i] = (char) (c + ('a' - 'A'));
            }
        }
        return new String(chars);
    }
}
