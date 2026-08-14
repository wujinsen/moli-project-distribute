package com.moli.knowledge.server.llm;

/**
 * Primary/fallback 可重试失败：超时、连接失败、HTTP 429/5xx（AI-8 §1.2）。
 */
public class LlmRetryableException extends Exception {

    private final Integer httpStatus;

    public LlmRetryableException(String message) {
        super(message);
        this.httpStatus = null;
    }

    public LlmRetryableException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public LlmRetryableException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = null;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }
}
