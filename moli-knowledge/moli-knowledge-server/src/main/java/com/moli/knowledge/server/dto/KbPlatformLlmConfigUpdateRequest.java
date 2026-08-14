package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@ApiModel("保存平台 LLM 配置")
public class KbPlatformLlmConfigUpdateRequest {

    @NotNull
    @ApiModelProperty(value = "总开关", required = true)
    private Boolean enabled;

    @NotBlank
    @ApiModelProperty(value = "提供方", required = true)
    private String provider;

    @NotBlank
    @ApiModelProperty(value = "OpenAI 兼容 base-url", required = true)
    private String baseUrl;

    @ApiModelProperty("api-key；空字符串表示不修改")
    private String apiKey;

    @ApiModelProperty("true 时清除 DB 中 api-key，回退 yaml")
    private Boolean clearApiKey;

    @NotBlank
    @ApiModelProperty(value = "默认模型", required = true)
    private String model;

    @ApiModelProperty("采样温度，默认 0.3")
    private Double temperature;

    @ApiModelProperty("超时秒，默认 90")
    private Integer timeoutSeconds;

    @ApiModelProperty("治理/Ingest 可选模型")
    private List<String> extraModels;
}
