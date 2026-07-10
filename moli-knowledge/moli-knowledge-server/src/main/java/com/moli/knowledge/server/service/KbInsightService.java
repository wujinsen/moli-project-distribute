package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.GraphVo;
import com.moli.knowledge.server.dto.LintIssueBatchAssignRequest;
import com.moli.knowledge.server.dto.LintIssueBatchStatusRequest;
import com.moli.knowledge.server.dto.LintVo;
import com.moli.knowledge.server.entity.KbLintIssue;

import java.util.List;

/**
 * 知识库图谱与体检。
 * graph 优先读 kb_relation（同步脚本落库），表为空时回退运行时解析 kb_document。
 * lint 运行时计算 broken/orphan/noSummary；scan 额外把结果落 kb_lint_issue 以便跟踪处理状态。
 */
public interface KbInsightService {

    /**
     * 关系图谱。大库下默认走 kb_relation 边表 + 轻量节点（不扫正文），并按度数裁剪。
     *
     * @param spaceId  空间ID，null=全部可读空间
     * @param mode     full（默认，返回裁剪后子图）/ summary（仅统计 + Top 枢纽）
     * @param maxNodes 最多返回节点数（按度数降序保留），&lt;=0 用默认值；summary 模式下为 topHubs 数量
     * @param minDeg   仅保留度数 &gt;= minDeg 的节点，null/0 不过滤
     */
    GraphVo graph(Long spaceId, String mode, Integer maxNodes, Integer minDeg);

    /**
     * 以某文档为中心的邻域子图（探索式，点击节点再拉）。
     *
     * @param spaceId  空间ID
     * @param docId    中心文档ID（必填）
     * @param depth    跳数 1~3，默认 1
     * @param maxNodes 子图节点上限
     */
    GraphVo ego(Long spaceId, Long docId, Integer depth, Integer maxNodes);

    /** 体检（只算不落库）：断链 / 孤儿页 / 缺摘要。 */
    LintVo lint(Long spaceId);

    /** 体检并落库 kb_lint_issue（清掉旧的待处理项后重建），返回本次结果。 */
    LintVo scan(Long spaceId);

    /** 查询已落库的体检问题（status 可空：0待处理/1已忽略/2已修复）。 */
    List<KbLintIssue> issues(Long spaceId, Integer status, String issueType, Long assigneeId, Integer priority);

    /** 更新某条体检问题的处理状态。 */
    void updateIssueStatus(Long id, Integer status);

    /** 批量更新体检问题状态（KBOPS-8）。 */
    int batchUpdateIssueStatus(LintIssueBatchStatusRequest request);

    /** 指派处理人 / 调整优先级（KBOPS-8）。 */
    void assignIssue(Long id, Long assigneeId, Integer priority);

    /** 批量指派处理人 / 优先级（KBOPS-8）。 */
    int batchAssignIssues(LintIssueBatchAssignRequest request);

    /** 定时任务调用 scan（无 ACL，仅调度器）。 */
    void scanScheduled(Long spaceId);
}
