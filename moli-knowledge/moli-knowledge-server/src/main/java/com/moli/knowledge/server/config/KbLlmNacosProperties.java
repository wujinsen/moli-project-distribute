package com.moli.knowledge.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LLM 配置在 Nacos 中的 DataId / Group（与 bootstrap extension-configs 一致）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "kb.llm.nacos")
public class KbLlmNacosProperties {

    /** 是否在 Nacos 中读写 kb.llm（false 时仅使用本地 application*.yml）。 */
    private boolean enabled = true;

    private String dataId = "knowledge-server-kb-llm-dev.yaml";

    private String group = "DEFAULT_GROUP";
}
