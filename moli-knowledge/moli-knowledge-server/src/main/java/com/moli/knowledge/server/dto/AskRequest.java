package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@ApiModel("知识库提问请求")
public class AskRequest {

    @NotBlank(message = "问题不能为空")
    @ApiModelProperty(value = "问题", required = true)
    private String question;

    @ApiModelProperty("空间ID（省略则全库；与 spaceIds 同时传时以 spaceIds 为准）")
    private Long spaceId;

    @ApiModelProperty("多空间ID（非空时在上述空间内检索，须均有读权限）")
    private List<Long> spaceIds;

    @ApiModelProperty("选用的候选页数上限，默认 8")
    private Integer topK = 8;
}
