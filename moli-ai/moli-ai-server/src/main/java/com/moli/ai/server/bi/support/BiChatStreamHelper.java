package com.moli.ai.server.bi.support;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SSE 解读 token 切分（契约 §1.1 token 事件）。
 */
public final class BiChatStreamHelper {

    private static final int TOKEN_CHUNK_SIZE = 6;

    private BiChatStreamHelper() {
    }

    public static List<String> splitExplanationTokens(String explanation) {
        if (!StringUtils.hasText(explanation)) {
            return Collections.emptyList();
        }
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < explanation.length(); ) {
            int end = Math.min(i + TOKEN_CHUNK_SIZE, explanation.length());
            tokens.add(explanation.substring(i, end));
            i = end;
        }
        return tokens;
    }
}
