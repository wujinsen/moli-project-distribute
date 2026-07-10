package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

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

    @ApiModelProperty("kb_llm_call_log 未接入时的占位说明")
    private String callLogNote = "kb_llm_call_log 未启用，调用率统计暂不可用";
}
