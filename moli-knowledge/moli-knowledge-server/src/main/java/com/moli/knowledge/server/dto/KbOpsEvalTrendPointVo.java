package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("检索质量日趋势点")
public class KbOpsEvalTrendPointVo {

    @ApiModelProperty("日期 yyyy-MM-dd")
    private String date;

    @ApiModelProperty("策略")
    private String strategy;

    @ApiModelProperty("hit@3")
    private BigDecimal hit3;

    @ApiModelProperty("MRR")
    private BigDecimal mrr;
}
