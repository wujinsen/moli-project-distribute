package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.GraphVo;
import com.moli.knowledge.server.dto.LintVo;
import com.moli.knowledge.server.entity.KbLintIssue;

import java.util.List;

/**
 * 知识库图谱与体检。
 * graph 优先读 kb_relation（同步脚本落库），表为空时回退运行时解析 kb_document。
 * lint 运行时计算 broken/orphan/noSummary；scan 额外把结果落 kb_lint_issue 以便跟踪处理状态。
 */
public interface KbInsightService {

    /** 关系图谱：节点=文档，连线优先取 kb_relation，缺省回退运行时（[[标题]] + 同标签）。 */
    GraphVo graph(Long spaceId);

    /** 体检（只算不落库）：断链 / 孤儿页 / 缺摘要。 */
    LintVo lint(Long spaceId);

    /** 体检并落库 kb_lint_issue（清掉旧的待处理项后重建），返回本次结果。 */
    LintVo scan(Long spaceId);

    /** 查询已落库的体检问题（status 可空：0待处理/1已忽略/2已修复）。 */
    List<KbLintIssue> issues(Long spaceId, Integer status);

    /** 更新某条体检问题的处理状态。 */
    void updateIssueStatus(Long id, Integer status);
}
