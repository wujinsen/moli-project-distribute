package com.moli.knowledge.server.util;

import lombok.Getter;

/**
 * 解析 Ingest 正文生成是否走 LLM；LLM 不可用时从 {@code useLlmGenerate=true} 自动降级模板模式。
 */
public final class IngestLlmGenerateModeUtil {

    public static final String FALLBACK_REASON =
            "LLM 未配置或已禁用，已自动改用模板模式（raw 直贴）";

    private IngestLlmGenerateModeUtil() {
    }

    public static Result resolve(boolean useLlmGenerate, boolean llmUsable) {
        if (!useLlmGenerate) {
            return new Result(false, false, null);
        }
        if (llmUsable) {
            return new Result(true, false, null);
        }
        return new Result(false, true, FALLBACK_REASON);
    }

    @Getter
    public static final class Result {
        private final boolean effectiveUseLlm;
        private final boolean llmFallback;
        private final String llmFallbackReason;

        Result(boolean effectiveUseLlm, boolean llmFallback, String llmFallbackReason) {
            this.effectiveUseLlm = effectiveUseLlm;
            this.llmFallback = llmFallback;
            this.llmFallbackReason = llmFallbackReason;
        }

        public boolean isTemplateMode() {
            return !effectiveUseLlm;
        }
    }
}
