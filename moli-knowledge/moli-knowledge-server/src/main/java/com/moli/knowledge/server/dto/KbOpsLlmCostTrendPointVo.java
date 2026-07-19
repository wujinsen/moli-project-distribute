package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("LLM 成本/缓存日趋势点（AI-8）")
public class KbOpsLlmCostTrendPointVo {

    @ApiModelProperty("日期 yyyy-MM-dd")
    private String date;

    @ApiModelProperty("当日估算成本 USD 合计")
    private double estimatedCostUsd;

    @ApiModelProperty("当日缓存命中次数")
    private int cacheHits;

    @ApiModelProperty("当日成功调用次数")
    private int calls;
}
