package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.KbOpsDashboardVo;
import com.moli.knowledge.server.service.KbOpsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/kb/ops")
@Api(tags = "知识库运维 Dashboard")
public class KbOpsController {

    @Resource
    private KbOpsService kbOpsService;

    @GetMapping("/dashboard")
    @ApiOperation("KBOPS-9 · 运维 Dashboard：Sync 趋势、Lint 工单、断链、LLM 可用性")
    public MoliResult<KbOpsDashboardVo> dashboard(@RequestParam(required = false) Long spaceId,
                                                  @RequestParam(required = false, defaultValue = "7") Integer trendDays) {
        return MoliResult.success(kbOpsService.dashboard(spaceId, trendDays));
    }
}
