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

    /**
     * Query(/kb/ask) 候选页召回上限。开启 fulltext 时先用 ngram 索引按相关度召回
     * 至多 N 页，再做内存 bigram 重排打分，避免把全空间已发布文档全量载入内存。
     */
    private int askCandidateLimit = 100;

    /** true：/kb/ask 按 kb_document_chunk 切段召回；false：整页召回（兼容回退）。 */
    private boolean chunkEnabled = true;

    public boolean fullTextEnabled() {
        return "fulltext".equalsIgnoreCase(mode);
    }

    public boolean isChunkEnabled() {
        return chunkEnabled;
    }

    public int normalizedAskCandidateLimit() {
        return askCandidateLimit <= 0 ? 100 : askCandidateLimit;
    }
}
