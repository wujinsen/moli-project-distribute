package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("测试平台 LLM 连通性（字段可选，缺省用已保存/生效配置）")
public class KbPlatformLlmConfigTestRequest {

    @ApiModelProperty("用户消息，默认 ping")
    private String message;

    private Boolean enabled;
    private String provider;
    private String baseUrl;
    @ApiModelProperty("测试用 api-key；空则使用已保存 key")
    private String apiKey;
    private String model;
    private Double temperature;
    private Integer timeoutSeconds;
    private List<String> extraModels;
}
