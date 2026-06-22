package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.AskRequest;
import com.moli.knowledge.server.dto.AskResponse;
import com.moli.knowledge.server.service.KbAskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/kb")
@Api(tags = "知识库问答")
public class KbAskController {

    @Resource
    private KbAskService kbAskService;

    @PostMapping("/ask")
    @ApiOperation("提问（检索选页→带引用作答；无 LLM key 时降级检索式）")
    public MoliResult<AskResponse> ask(@Validated @RequestBody AskRequest request) {
        return MoliResult.success(kbAskService.ask(request));
    }
}
