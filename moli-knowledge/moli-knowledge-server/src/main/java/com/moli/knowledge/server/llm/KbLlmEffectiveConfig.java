package com.moli.knowledge.server.llm;

import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 内存中的 LLM 生效配置快照（含明文 apiKey，仅服务端使用）。
 */
@Value
@Builder
public class KbLlmEffectiveConfig {

    boolean enabled;
    String provider;
    String baseUrl;
    String apiKey;
    String apiKeyMask;
    String model;
    double temperature;
    int timeoutSeconds;
    @Builder.Default
    List<String> extraModels = Collections.emptyList();
    KbLlmConfigSource source;

    public boolean apiKeyConfigured() {
        return StringUtils.isNotBlank(apiKey);
    }

    public boolean usable() {
        return enabled && apiKeyConfigured();
    }
}
