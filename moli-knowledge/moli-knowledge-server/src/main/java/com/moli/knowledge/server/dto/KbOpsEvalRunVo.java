package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("单次评测 run 明细")
public class KbOpsEvalRunVo {

    private Long id;

    @ApiModelProperty("报告 time")
    private Date runAt;

    private String strategy;
    private Integer useLlm;
    private Integer goldenTotal;
    private Integer answerableTotal;
    private Integer negativeTotal;
    private Integer errors;

    private BigDecimal hit1;
    private BigDecimal hit3;
    private BigDecimal hit5;
    private BigDecimal hit8;
    private BigDecimal mrr;
    private BigDecimal coverage;
    private BigDecimal refusalAccuracy;
    private Integer p95Ms;

    @ApiModelProperty("by_difficulty 原样 JSON")
    private String byDifficultyJson;

    private String reportPath;
    private String gitSha;
    private Boolean gatePass;
    private Date createTime;
}
