package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("LLM 问答能力（不含 api-key；开关由请求 useLlm 控制）")
public class KbLlmConfigVo {

    @ApiModelProperty("后端是否已配置且可调用 LLM（kb.llm.enabled + api-key）")
    private boolean available;

    @ApiModelProperty("配置文件 kb.llm.enabled")
    private boolean configEnabled;

    @ApiModelProperty("是否已配置 api-key")
    private boolean apiKeyConfigured;

    @ApiModelProperty("提供方 deepseek/qwen/glm")
    private String provider;

    @ApiModelProperty("模型名")
    private String model;
}
