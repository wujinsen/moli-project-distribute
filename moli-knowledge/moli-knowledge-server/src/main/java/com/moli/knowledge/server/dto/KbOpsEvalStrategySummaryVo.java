package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("单策略检索质量摘要")
public class KbOpsEvalStrategySummaryVo {

    @ApiModelProperty("策略")
    private String strategy;

    @ApiModelProperty("最近一次 run_at")
    private Date latestRunAt;

    private BigDecimal hit1;
    private BigDecimal hit3;
    private BigDecimal hit5;
    private BigDecimal mrr;

    @ApiModelProperty("P95 延迟毫秒")
    private Integer p95Ms;

    @ApiModelProperty("错误数")
    private Integer errors;

    @ApiModelProperty("基线 hit@3")
    private BigDecimal baselineHit3;

    @ApiModelProperty("latest hit@3 − baseline")
    private BigDecimal deltaHit3;

    @ApiModelProperty("最近一次门禁是否通过")
    private Boolean gatePass;
}
