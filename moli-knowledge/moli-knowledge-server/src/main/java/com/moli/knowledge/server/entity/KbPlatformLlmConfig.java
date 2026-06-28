package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 平台级 LLM 配置单例行（T19 · id 固定为 1）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_platform_llm_config")
@ApiModel("知识库平台 LLM 配置")
public class KbPlatformLlmConfig extends BaseEntity {

    public static final long SINGLETON_ID = 1L;

    @ApiModelProperty("1启用 0停用")
    private Integer enabled;

    @ApiModelProperty("deepseek/qwen/glm/custom")
    private String provider;

    @ApiModelProperty("OpenAI 兼容 base-url")
    private String baseUrl;

    @ApiModelProperty("AES-GCM 密文 Base64")
    private String apiKeyCipher;

    @ApiModelProperty("脱敏展示")
    private String apiKeyMask;

    @ApiModelProperty("默认模型")
    private String model;

    @ApiModelProperty("采样温度")
    private BigDecimal temperature;

    @ApiModelProperty("HTTP 超时秒")
    private Integer timeoutSeconds;

    @ApiModelProperty("治理/Ingest 可选模型 JSON 数组")
    private String extraModels;
}
