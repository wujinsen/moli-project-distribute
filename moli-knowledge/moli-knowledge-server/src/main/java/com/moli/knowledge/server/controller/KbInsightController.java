package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.GraphVo;
import com.moli.knowledge.server.dto.LintVo;
import com.moli.knowledge.server.entity.KbLintIssue;
import com.moli.knowledge.server.service.KbInsightService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    @ApiOperation("关系图谱（节点=文档，连线=正文[[标题]]引用 + 同标签）")
    public MoliResult<GraphVo> graph(@RequestParam(required = false) Long spaceId) {
        return MoliResult.success(kbInsightService.graph(spaceId));
    }

    @GetMapping("/lint")
    @ApiOperation("体检（断链 / 孤儿页 / 缺摘要）——只算不落库")
    public MoliResult<LintVo> lint(@RequestParam(required = false) Long spaceId) {
        return MoliResult.success(kbInsightService.lint(spaceId));
    }

    @PostMapping("/lint/scan")
    @ApiOperation("体检并落库 kb_lint_issue（清旧待处理项后重建）")
    public MoliResult<LintVo> scan(@RequestParam(required = false) Long spaceId) {
        return MoliResult.success(kbInsightService.scan(spaceId));
    }

    @GetMapping("/lint/issues")
    @ApiOperation("查询已落库的体检问题（status 可空：0待处理/1已忽略/2已修复）")
    public MoliResult<List<KbLintIssue>> issues(@RequestParam(required = false) Long spaceId,
                                                @RequestParam(required = false) Integer status) {
        return MoliResult.success(kbInsightService.issues(spaceId, status));
    }

    @PutMapping("/lint/issue/{id}")
    @ApiOperation("更新体检问题处理状态（0待处理/1已忽略/2已修复）")
    public MoliResult<Boolean> updateIssue(@PathVariable Long id, @RequestParam Integer status) {
        kbInsightService.updateIssueStatus(id, status);
        return MoliResult.success(Boolean.TRUE);
    }
}
