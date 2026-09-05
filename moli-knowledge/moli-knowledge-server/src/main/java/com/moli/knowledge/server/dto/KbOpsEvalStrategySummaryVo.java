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

    @ApiModelProperty("是否已有评测 run；false 时 gatePass 必为 null，勿展示「未通过」")
    private boolean hasLatestRun;

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

    @ApiModelProperty("最近一次门禁：true 通过 / false 未通过 / null 无评测或落库时未判定")
    private Boolean gatePass;
}
