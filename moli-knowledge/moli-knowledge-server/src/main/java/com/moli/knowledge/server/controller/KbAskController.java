package com.moli.knowledge.server.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.AgenticAskRequest;
import com.moli.knowledge.server.dto.AgenticAskVo;
import com.moli.knowledge.server.dto.AskRequest;
import com.moli.knowledge.server.dto.AskResponse;
import com.moli.knowledge.server.dto.KbLlmConfigVo;
import com.moli.knowledge.server.dto.QaHistoryVo;
import com.moli.knowledge.server.service.KbAgenticAskService;
import com.moli.knowledge.server.service.KbAskService;
import com.moli.knowledge.server.service.KbLlmConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/kb")
@Api(tags = "知识库问答")
public class KbAskController {

    @Resource
    private KbAskService kbAskService;
    @Resource
    private KbAgenticAskService kbAgenticAskService;
    @Resource
    private KbLlmConfigService kbLlmConfigService;

    @GetMapping("/ask/llm-config")
    @ApiOperation("LLM 后端能力探测（不含 api-key；是否调用见 POST /kb/ask 的 useLlm）")
    public MoliResult<KbLlmConfigVo> llmConfig() {
        return MoliResult.success(kbLlmConfigService.getConfig());
    }

    @PostMapping("/ask")
    @ApiOperation("提问（检索选页→带引用作答；无 LLM key 时降级检索式）")
    public MoliResult<AskResponse> ask(@Validated @RequestBody AskRequest request) {
        return MoliResult.success(kbAskService.ask(request));
    }

    @PostMapping("/ask/agentic")
    @ApiOperation("Agentic 提问（改写/拆解→多轮检索→生成；kb.agentic.enabled=false 时退化单轮）")
    public MoliResult<AgenticAskVo> agenticAsk(@Validated @RequestBody AgenticAskRequest request) {
        return MoliResult.success(kbAgenticAskService.agenticAsk(request));
    }

    @GetMapping("/ask/history")
    @ApiOperation("我的问答历史")
    public MoliResult<Page<QaHistoryVo>> history(@RequestParam(required = false) Long spaceId,
                                                 @RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "10") int pageSize) {
        return MoliResult.success(kbAskService.history(spaceId, pageNum, pageSize));
    }

    @PutMapping("/ask/feedback/{id}")
    @ApiOperation("问答反馈（useful: 1有用 0无用）")
    public MoliResult<Boolean> feedback(@PathVariable Long id, @RequestParam Integer useful) {
        kbAskService.feedback(id, useful);
        return MoliResult.success(Boolean.TRUE);
    }
}
