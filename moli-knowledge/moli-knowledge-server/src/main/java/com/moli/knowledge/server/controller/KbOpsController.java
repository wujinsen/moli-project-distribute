package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.KbOpsDashboardVo;
import com.moli.knowledge.server.dto.KbOpsEvalRunVo;
import com.moli.knowledge.server.dto.KbOpsEvalTrendPointVo;
import com.moli.knowledge.server.service.KbOpsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/kb/ops")
@Api(tags = "知识库运维 Dashboard")
public class KbOpsController {

    @Resource
    private KbOpsService kbOpsService;

    @GetMapping("/dashboard")
    @ApiOperation("KBOPS-9 · 运维 Dashboard：Sync 趋势、Lint 工单、断链、LLM 调用率、wiki↔DB 漂移")
    public MoliResult<KbOpsDashboardVo> dashboard(@RequestParam(required = false) Long spaceId,
                                                  @RequestParam(required = false, defaultValue = "7") Integer trendDays) {
        return MoliResult.success(kbOpsService.dashboard(spaceId, trendDays));
    }

    @GetMapping("/eval-trend")
    @ApiOperation("AI-3 · 检索质量 hit@3/MRR 日趋势（按日取最后一次 run）")
    public MoliResult<List<KbOpsEvalTrendPointVo>> evalTrend(
            @RequestParam(required = false) String strategy,
            @RequestParam(required = false, defaultValue = "14") Integer days) {
        return MoliResult.success(kbOpsService.evalTrend(strategy, days));
    }

    @GetMapping("/eval-runs")
    @ApiOperation("AI-3 · 检索质量 run 明细（含 report_path / gate_pass）")
    public MoliResult<List<KbOpsEvalRunVo>> evalRuns(
            @RequestParam(required = false) String strategy,
            @RequestParam(required = false, defaultValue = "20") Integer limit) {
        return MoliResult.success(kbOpsService.evalRuns(strategy, limit));
    }
}
