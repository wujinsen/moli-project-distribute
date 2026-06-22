package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel("知识库提问请求")
public class AskRequest {

    @NotBlank(message = "问题不能为空")
    @ApiModelProperty(value = "问题", required = true)
    private String question;

    @ApiModelProperty("空间ID（省略则全库）")
    private Long spaceId;

    @ApiModelProperty("选用的候选页数上限，默认 8")
    private Integer topK = 8;
}
