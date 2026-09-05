package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.KbOpsDashboardVo;
import com.moli.knowledge.server.dto.KbOpsEvalRunVo;
import com.moli.knowledge.server.dto.KbOpsEvalTrendPointVo;

import java.util.List;

/**
 * 知识库运维 Dashboard（KBOPS-9）。
 */
public interface KbOpsService {

    /**
     * 聚合 Sync 趋势、Lint 工单、断链、LLM 可用性。
     *
     * @param spaceId      空间 ID，null=全部可读空间
     * @param trendDays    趋势天数（默认 7，最大 30）
     * @param includeDrift 是否同步扫描 wiki↔DB 漂移（默认 false；漂移请用 GET /kb/sync/drift）
     */
    KbOpsDashboardVo dashboard(Long spaceId, Integer trendDays, boolean includeDrift);

    /**
     * 检索质量按日趋势（AI-3）。
     *
     * @param strategy 策略，null=全档
     * @param days     默认 14，最大 90
     */
    List<KbOpsEvalTrendPointVo> evalTrend(String strategy, Integer days);

    /**
     * 检索质量 run 明细列表（AI-3）。
     *
     * @param strategy 策略，null=全档
     * @param limit    默认 20，最大 100
     */
    List<KbOpsEvalRunVo> evalRuns(String strategy, Integer limit);
}
