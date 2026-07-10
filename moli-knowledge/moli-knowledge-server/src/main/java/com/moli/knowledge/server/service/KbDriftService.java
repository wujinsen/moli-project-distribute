package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.KbDriftReportVo;
import com.moli.knowledge.server.dto.KbOpsDriftSummaryVo;

/**
 * KBOPS-A3 · wiki 磁盘与 kb_document 漂移检测。
 */
public interface KbDriftService {

    /**
     * 单空间漂移报告。
     *
     * @param spaceId 必填
     * @param sampleLimit 每类样本上限，默认 20，最大 100
     */
    KbDriftReportVo drift(Long spaceId, Integer sampleLimit);

    /**
     * 多空间漂移摘要（Dashboard / 运维巡检）。
     *
     * @param spaceId null=全部可读且已配置 wiki 的空间
     */
    KbOpsDriftSummaryVo driftSummary(Long spaceId, Integer sampleLimit);
}
