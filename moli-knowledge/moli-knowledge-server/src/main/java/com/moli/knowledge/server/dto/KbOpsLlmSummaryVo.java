package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("LLM 可用性摘要（Dashboard）")
public class KbOpsLlmSummaryVo {

    @ApiModelProperty("总开关")
    private Boolean enabled;

    @ApiModelProperty("当前是否可调用")
    private Boolean available;

    @ApiModelProperty("provider")
    private String provider;

    @ApiModelProperty("默认模型")
    private String model;

    @ApiModelProperty("生效来源 database/yaml_fallback")
    private String source;

    @ApiModelProperty("是否写入 kb_llm_call_log")
    private Boolean callLogEnabled;

    @ApiModelProperty("统计窗口天数")
    private Integer trendDays;

    @ApiModelProperty("窗口内总调用次数")
    private long totalCalls;

    @ApiModelProperty("成功次数")
    private long successCalls;

    @ApiModelProperty("失败次数")
    private long failCalls;

    @ApiModelProperty("成功率 0~1")
    private Double successRate;

    @ApiModelProperty("失败率 0~1")
    private Double failRate;

    @ApiModelProperty("按 scene 计数")
    private Map<String, Long> callsByScene = new LinkedHashMap<>();

    @ApiModelProperty("近 N 日调用趋势")
    private List<KbOpsLlmCallTrendPointVo> callTrend = new ArrayList<>();
}
