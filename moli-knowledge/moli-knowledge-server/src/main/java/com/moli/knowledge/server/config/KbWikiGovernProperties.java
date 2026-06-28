package com.moli.knowledge.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Wiki 治理工作台（T16）：批量 AI 修复走 {@code kb.llm} OpenAI 兼容接口。
 */
@Data
@RefreshScope
@Component
@ConfigurationProperties(prefix = "kb.wiki.govern")
public class KbWikiGovernProperties {

    /**
     * 治理页可选模型列表（下拉）；为空则仅用 {@link KbLlmProperties#getModel()}。
     */
    private List<String> models = new ArrayList<>();
}
