package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("Wiki 治理 · 单条合并提示")
public class WikiGovernMergeHintItemVo {

    @ApiModelProperty("issue.kind")
    private String kind;

    @ApiModelProperty("主 slug（dup_content 有值；dup_slug 可能为空）")
    private String page;

    @ApiModelProperty("lint detail 原文")
    private String detail;

    @ApiModelProperty("冲突/重复的其它 wiki slug")
    private List<String> relatedSlugs;

    @ApiModelProperty("建议保留的 canonical slug")
    private String canonicalSlug;

    @ApiModelProperty("复制到 Cursor / IDE 的一键指令")
    private String cursorPrompt;

    @ApiModelProperty("人工步骤（UI 列表展示）")
    private List<String> manualSteps;
}
