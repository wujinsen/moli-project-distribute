package com.moli.knowledge.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * /kb/ask 引用与 LLM 上下文默认上限（与 {@code AskRequest.topK}/{@code llmContextTopK} 配合）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "kb.ask")
public class KbAskProperties {

    /** 返回给前端的 citations 条数上限（页级）。 */
    private int citationTopK = 8;

    /** 拼进 LLM prompt 的页/文档数上限（chunk 模式下每页至多 2 段）。 */
    private int llmContextTopK = 3;

    /** LLM 上下文总字符 budget（中英文混排近似 token 控制）。 */
    private int llmContextMaxChars = 12000;

    public int normalizedCitationTopK() {
        return citationTopK <= 0 ? 8 : citationTopK;
    }

    public int normalizedLlmContextTopK() {
        return llmContextTopK <= 0 ? 3 : llmContextTopK;
    }

    public int normalizedLlmContextMaxChars() {
        return llmContextMaxChars <= 0 ? 12000 : llmContextMaxChars;
    }
}
