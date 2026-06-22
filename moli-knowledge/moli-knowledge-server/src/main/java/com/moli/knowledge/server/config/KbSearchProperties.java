package com.moli.knowledge.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文档检索模式。默认使用 MySQL ngram 全文索引（表上 ftx_kb_document）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "kb.search")
public class KbSearchProperties {

    /** fulltext：MATCH AGAINST；like：三字段 LIKE（兼容旧行为）。 */
    private String mode = "fulltext";

    public boolean fullTextEnabled() {
        return "fulltext".equalsIgnoreCase(mode);
    }
}
