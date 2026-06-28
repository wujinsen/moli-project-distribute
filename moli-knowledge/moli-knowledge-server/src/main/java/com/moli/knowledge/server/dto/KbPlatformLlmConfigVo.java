package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("平台 LLM 配置（管理视图，不含明文 api-key）")
public class KbPlatformLlmConfigVo {

    @ApiModelProperty("总开关")
    private Boolean enabled;

    @ApiModelProperty("deepseek/qwen/glm/custom")
    private String provider;

    @ApiModelProperty("OpenAI 兼容 base-url")
    private String baseUrl;

    @ApiModelProperty("是否已配置 api-key")
    private Boolean apiKeyConfigured;

    @ApiModelProperty("脱敏 api-key")
    private String apiKeyMask;

    @ApiModelProperty("默认模型")
    private String model;

    @ApiModelProperty("采样温度")
    private Double temperature;

    @ApiModelProperty("HTTP 超时秒")
    private Integer timeoutSeconds;

    @ApiModelProperty("治理/Ingest 可选模型")
    private List<String> extraModels;

    @ApiModelProperty("当前运行时是否可调用 LLM")
    private Boolean available;

    @ApiModelProperty("当前生效来源：database / yaml_fallback")
    private String source;

    @ApiModelProperty("DB 是否已持久化 api-key")
    private Boolean persistedInDatabase;

    @ApiModelProperty("DB 更新时间 yyyy-MM-dd HH:mm:ss")
    private String updateTime;
}
