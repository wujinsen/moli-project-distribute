package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.KbOpsDashboardVo;

/**
 * 知识库运维 Dashboard（KBOPS-9）。
 */
public interface KbOpsService {

    /**
     * 聚合 Sync 趋势、Lint 工单、断链、LLM 可用性。
     *
     * @param spaceId   空间 ID，null=全部可读空间
     * @param trendDays 趋势天数（默认 7，最大 30）
     */
    KbOpsDashboardVo dashboard(Long spaceId, Integer trendDays);
}
