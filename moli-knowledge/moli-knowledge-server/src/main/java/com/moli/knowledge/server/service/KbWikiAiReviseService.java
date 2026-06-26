package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.WikiAiReviseRequest;
import com.moli.knowledge.server.dto.WikiAiReviseResultVo;
import com.moli.knowledge.server.dto.WikiLintPreviewRequest;
import com.moli.knowledge.server.dto.WikiLintPreviewVo;

public interface KbWikiAiReviseService {

    /** AI 改稿建议（不写盘）。 */
    WikiAiReviseResultVo aiRevise(WikiAiReviseRequest request);

    /** 保存前轻量 lint 预检（断链 / frontmatter 等）。 */
    WikiLintPreviewVo previewLint(WikiLintPreviewRequest request);
}
