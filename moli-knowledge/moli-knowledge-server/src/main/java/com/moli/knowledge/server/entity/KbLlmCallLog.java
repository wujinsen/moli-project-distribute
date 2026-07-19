package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("kb_llm_call_log")
@ApiModel("LLM 调用审计")
public class KbLlmCallLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("触发用户ID")
    private Long userId;

    @ApiModelProperty("场景")
    private String scene;

    @ApiModelProperty("provider")
    private String provider;

    @ApiModelProperty("模型")
    private String model;

    @ApiModelProperty("success/fail")
    private String status;

    @ApiModelProperty("耗时毫秒")
    private Integer latencyMs;

    @ApiModelProperty("语义缓存命中")
    private Boolean cacheHit;

    @ApiModelProperty("经 fallback 成功")
    private Boolean failover;

    @ApiModelProperty("估算 prompt tokens")
    private Integer promptTokensEst;

    @ApiModelProperty("估算 completion tokens")
    private Integer completionTokensEst;

    @ApiModelProperty("估算成本 USD")
    private java.math.BigDecimal estimatedCostUsd;

    @ApiModelProperty("失败摘要")
    private String errorMessage;

    @ApiModelProperty("调用时间")
    private Date createTime;
}
