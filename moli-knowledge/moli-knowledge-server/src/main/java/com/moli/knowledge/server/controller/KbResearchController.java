package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.ResearchRequest;
import com.moli.knowledge.server.dto.ResearchStartVo;
import com.moli.knowledge.server.dto.ResearchVo;
import com.moli.knowledge.server.service.KbResearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;

@RestController
@RequestMapping("/kb")
@Api(tags = "知识库 DeepResearch")
public class KbResearchController {

    @Resource
    private KbResearchService kbResearchService;

    @PostMapping("/research")
    @ApiOperation("启动主题调研（异步，返回 runId；Planner→Retriever→Writer→Reviewer）")
    public MoliResult<ResearchStartVo> research(
            @Validated @RequestBody ResearchRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return MoliResult.success(kbResearchService.start(request, authorization));
    }

    @PostMapping("/research/start")
    @ApiOperation("启动主题调研（同 POST /kb/research）")
    public MoliResult<ResearchStartVo> researchStart(
            @Validated @RequestBody ResearchRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return MoliResult.success(kbResearchService.start(request, authorization));
    }

    @GetMapping("/research/{runId}")
    @ApiOperation("查询调研状态与结果摘要")
    public MoliResult<ResearchVo> getRun(@PathVariable String runId) {
        return MoliResult.success(kbResearchService.getRun(runId));
    }

    @GetMapping(value = "/research/{runId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ApiOperation("SSE 进度：progress / complete / error")
    public SseEmitter stream(@PathVariable String runId) {
        return kbResearchService.stream(runId);
    }
}
