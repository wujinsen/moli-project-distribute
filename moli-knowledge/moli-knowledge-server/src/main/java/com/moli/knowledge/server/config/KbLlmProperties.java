package com.moli.knowledge.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Query(/kb/ask) 使用的 LLM 配置。OpenAI 兼容接口（DeepSeek / Qwen / GLM 等）。
 * 主配置在 Nacos {@code knowledge-server-kb-llm-dev.yaml}；{@link RefreshScope} 支持热更新。
 * {@code enabled=false} 或 api-key 为空时 /kb/ask 自动降级为「检索式」答案。
 */
@Data
@RefreshScope
@Component
@ConfigurationProperties(prefix = "kb.llm")
public class KbLlmProperties {

    /** 是否启用生成式（需同时配置 apiKey）。 */
    private boolean enabled = false;

    /** 提供方标识：deepseek / qwen / glm。 */
    private String provider = "deepseek";

    /** OpenAI 兼容 base-url，如 https://api.deepseek.com/v1 。 */
    private String baseUrl = "https://api.deepseek.com/v1";

    /** API Key。留空则降级检索式。 */
    private String apiKey = "";

    /** 模型名，如 deepseek-chat / qwen-plus / glm-4-air 。 */
    private String model = "deepseek-chat";

    /** 采样温度。 */
    private Double temperature = 0.3;

    /** 调用超时（秒）。 */
    private Integer timeoutSeconds = 90;

    public boolean usable() {
        return enabled && apiKey != null && !apiKey.trim().isEmpty();
    }
}
