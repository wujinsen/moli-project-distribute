package com.moli.ai.server.bi.support;

/**
 * sidecar 不可用（§1.2 降级 → 10602）。
 */
public class BiAgentUnavailableException extends RuntimeException {

    public BiAgentUnavailableException(String message) {
        super(message);
    }

    public BiAgentUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
