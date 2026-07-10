package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.GraphVo;
import com.moli.knowledge.server.dto.LintVo;
import com.moli.knowledge.server.dto.LintIssueBatchAssignRequest;
import com.moli.knowledge.server.dto.LintIssueBatchStatusRequest;
import com.moli.knowledge.server.support.KbLintIssueTypes;
import com.moli.knowledge.server.entity.KbLintIssue;
import com.moli.knowledge.server.service.KbInsightService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/kb")
@Api(tags = "知识库图谱与体检")
public class KbInsightController {

    @Resource
    private KbInsightService kbInsightService;

    @GetMapping("/graph")
    @ApiOperation("关系图谱（节点=文档，边读 kb_relation；大库按度数裁剪，含 meta 统计）")
    public MoliResult<GraphVo> graph(@RequestParam(required = false) Long spaceId,
                                     @RequestParam(required = false, defaultValue = "full") String mode,
                                     @RequestParam(required = false) Integer maxNodes,
                                     @RequestParam(required = false) Integer minDeg) {
        return MoliResult.success(kbInsightService.graph(spaceId, mode, maxNodes, minDeg));
    }

    @GetMapping("/graph/ego")
    @ApiOperation("某文档为中心的邻域子图（探索式，点击节点再拉 1~3 跳）")
    public MoliResult<GraphVo> graphEgo(@RequestParam(required = false) Long spaceId,
                                        @RequestParam Long docId,
                                        @RequestParam(required = false, defaultValue = "1") Integer depth,
                                        @RequestParam(required = false) Integer maxNodes) {
        return MoliResult.success(kbInsightService.ego(spaceId, docId, depth, maxNodes));
    }

    @GetMapping("/lint")
    @ApiOperation("体检（对齐 lint.py：断链/孤儿/frontmatter/duplicate/stale/conflict 等）——只算不落库")
    public MoliResult<LintVo> lint(@RequestParam(required = false) Long spaceId) {
        return MoliResult.success(kbInsightService.lint(spaceId));
    }

    @PostMapping("/lint/scan")
    @ApiOperation("体检并落库 kb_lint_issue（清旧待处理项后重建）")
    public MoliResult<LintVo> scan(@RequestParam(required = false) Long spaceId) {
        return MoliResult.success(kbInsightService.scan(spaceId));
    }

    @GetMapping("/lint/issues")
    @ApiOperation("查询已落库的体检问题（支持 type/assignee/priority 筛选）")
    public MoliResult<List<KbLintIssue>> issues(@RequestParam(required = false) Long spaceId,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(required = false) String issueType,
                                                @RequestParam(required = false) Long assigneeId,
                                                @RequestParam(required = false) Integer priority) {
        return MoliResult.success(kbInsightService.issues(spaceId, status, issueType, assigneeId, priority));
    }

    @PutMapping("/lint/issues/batch-status")
    @ApiOperation("KBOPS-8 · 批量更新体检问题状态")
    public MoliResult<Integer> batchUpdateIssueStatus(@RequestBody LintIssueBatchStatusRequest request) {
        return MoliResult.success(kbInsightService.batchUpdateIssueStatus(request));
    }

    @PutMapping("/lint/issues/batch-assign")
    @ApiOperation("KBOPS-8 · 批量指派处理人 / 调整优先级")
    public MoliResult<Integer> batchAssignIssues(@RequestBody LintIssueBatchAssignRequest request) {
        return MoliResult.success(kbInsightService.batchAssignIssues(request));
    }

    @GetMapping("/lint/issue-types")
    @ApiOperation("KBOPS-8 · 支持的体检问题类型")
    public MoliResult<List<String>> issueTypes() {
        return MoliResult.success(KbLintIssueTypes.all());
    }

    @PutMapping("/lint/issue/{id}/assign")
    @ApiOperation("KBOPS-8 · 指派处理人 / 调整优先级")
    public MoliResult<Boolean> assignIssue(@PathVariable Long id,
                                           @RequestParam(required = false) Long assigneeId,
                                           @RequestParam(required = false) Integer priority) {
        kbInsightService.assignIssue(id, assigneeId, priority);
        return MoliResult.success(Boolean.TRUE);
    }

    @PutMapping("/lint/issue/{id}")
    @ApiOperation("更新体检问题处理状态（0待处理/1已忽略/2已修复）")
    public MoliResult<Boolean> updateIssue(@PathVariable Long id, @RequestParam Integer status) {
        kbInsightService.updateIssueStatus(id, status);
        return MoliResult.success(Boolean.TRUE);
    }
}
