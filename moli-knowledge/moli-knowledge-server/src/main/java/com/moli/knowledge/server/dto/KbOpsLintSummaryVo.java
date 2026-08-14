package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@ApiModel("Lint 工单汇总")
public class KbOpsLintSummaryVo {

    @ApiModelProperty("待处理总数（status=0）")
    private long openCount;

    @ApiModelProperty("已忽略（status=1）")
    private long ignoredCount;

    @ApiModelProperty("已修复（status=2）")
    private long fixedCount;

    @ApiModelProperty("按 issue_type 的待处理数")
    private Map<String, Long> openByType = new LinkedHashMap<>();

    @ApiModelProperty("broken_link 待处理 Top N（detail 摘要）")
    private java.util.List<String> topBrokenLinks = new java.util.ArrayList<>();
}
