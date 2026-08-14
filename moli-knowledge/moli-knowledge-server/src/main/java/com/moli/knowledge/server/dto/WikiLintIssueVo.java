package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("文件 Lint 单条问题")
public class WikiLintIssueVo {

    @ApiModelProperty("严重级别：error / warn / info")
    private String level;

    @ApiModelProperty("问题类型：broken_link / orphan / missing_source / related_overflow ...")
    private String kind;

    @ApiModelProperty("页 slug（= 修复目标，可直接喂给 enrich / ai-revise）")
    private String page;

    @ApiModelProperty("问题明细")
    private String detail;

    @ApiModelProperty("修复建议")
    private String suggest;
}
