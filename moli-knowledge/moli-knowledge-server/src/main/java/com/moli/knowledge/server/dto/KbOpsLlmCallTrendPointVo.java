package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("LLM 调用日趋势点")
public class KbOpsLlmCallTrendPointVo {

    @ApiModelProperty("日期 yyyy-MM-dd")
    private String date;

    @ApiModelProperty("成功调用次数")
    private int successCalls;

    @ApiModelProperty("失败调用次数")
    private int failCalls;
}
