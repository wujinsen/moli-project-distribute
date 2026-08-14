package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("平台 LLM 连通性测试结果")
public class KbPlatformLlmConfigTestResultVo {

    @ApiModelProperty("是否成功")
    private Boolean success;

    @ApiModelProperty("耗时毫秒")
    private Long latencyMs;

    @ApiModelProperty("实际使用的模型")
    private String model;

    @ApiModelProperty("回复摘要（截断）")
    private String replyPreview;

    @ApiModelProperty("失败原因")
    private String error;
}
