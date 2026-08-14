package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.ResearchVo;

/**
 * AI-10 DeepResearch Ingest 回写（D-INV-1）。
 */
public interface KbResearchWritebackService {

    /**
     * @return ingestJobId 与 wiki 相对 outputPath（develop/outputs/{slug}）
     */
    WritebackResult writeback(ResearchVo result, String topic, Long spaceIdOverride);

    final class WritebackResult {
        public final Long ingestJobId;
        public final String outputPath;

        public WritebackResult(Long ingestJobId, String outputPath) {
            this.ingestJobId = ingestJobId;
            this.outputPath = outputPath;
        }
    }
}
