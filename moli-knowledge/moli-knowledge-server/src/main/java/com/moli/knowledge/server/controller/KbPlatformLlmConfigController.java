package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.KbPlatformLlmConfigTestRequest;
import com.moli.knowledge.server.dto.KbPlatformLlmConfigTestResultVo;
import com.moli.knowledge.server.dto.KbPlatformLlmConfigUpdateRequest;
import com.moli.knowledge.server.dto.KbPlatformLlmConfigVo;
import com.moli.knowledge.server.service.KbPlatformLlmConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/kb/platform")
@Api(tags = "知识库平台设置")
public class KbPlatformLlmConfigController {

    @Resource
    private KbPlatformLlmConfigService kbPlatformLlmConfigService;

    @GetMapping("/llm-config")
    @ApiOperation("读取平台 LLM 配置（脱敏；需 kb:platform:llm 或平台超管）")
    public MoliResult<KbPlatformLlmConfigVo> getConfig() {
        return MoliResult.success(kbPlatformLlmConfigService.getAdminView());
    }

    @PutMapping("/llm-config")
    @ApiOperation("保存平台 LLM 配置（api-key 加密入库；保存后热刷新 Runtime）")
    public MoliResult<KbPlatformLlmConfigVo> saveConfig(@Validated @RequestBody KbPlatformLlmConfigUpdateRequest request) {
        return MoliResult.success(kbPlatformLlmConfigService.save(request));
    }

    @PostMapping("/llm-config/test")
    @ApiOperation("测试 LLM 连通性（可用未保存的表单值覆盖）")
    public MoliResult<KbPlatformLlmConfigTestResultVo> testConfig(
            @RequestBody(required = false) KbPlatformLlmConfigTestRequest request) {
        return MoliResult.success(kbPlatformLlmConfigService.testConnection(request));
    }
}
