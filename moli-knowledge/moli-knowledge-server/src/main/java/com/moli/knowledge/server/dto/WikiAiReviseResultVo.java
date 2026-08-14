package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Wiki AI 改稿结果")
public class WikiAiReviseResultVo {

    @ApiModelProperty("建议全文（含 frontmatter）")
    private String suggestedContent;

    @ApiModelProperty("LLM 提供方")
    private String provider;

    @ApiModelProperty("模型名")
    private String model;

    @ApiModelProperty("AI 修改说明（可选）")
    private String notes;
}
