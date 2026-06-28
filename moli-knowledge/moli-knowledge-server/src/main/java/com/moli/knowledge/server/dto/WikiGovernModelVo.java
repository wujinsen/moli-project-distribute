package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Wiki 治理可选模型")
public class WikiGovernModelVo {

    @ApiModelProperty("模型 ID（传给 ai-revise.model）")
    private String id;

    @ApiModelProperty("展示名")
    private String displayName;
}
