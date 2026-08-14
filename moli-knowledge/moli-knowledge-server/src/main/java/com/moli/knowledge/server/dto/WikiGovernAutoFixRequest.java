package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("Wiki 治理 · 一键修复请求（脚本 → AI → 复检 → 可选 Sync）")
public class WikiGovernAutoFixRequest {

    @ApiModelProperty(value = "空间 ID", required = true)
    private Long spaceId;

    @ApiModelProperty(value = "Lint 勾选的问题；空=仅 relint/sync", required = true)
    private List<WikiLintIssueVo> issues;

    @ApiModelProperty("AI 模型；默认 kb.llm.model")
    private String model;

    @ApiModelProperty("执行脚本修复，默认 true")
    private Boolean scriptFix;

    @ApiModelProperty("对剩余可 AI 项批量修复，默认 true")
    private Boolean aiFix;

    @ApiModelProperty("修复后自动 lint-space，默认 true")
    private Boolean relintAfter;

    @ApiModelProperty("复检 strict 模式")
    private Boolean strict;

    @ApiModelProperty("修复后 Sync 写库，默认 false")
    private Boolean syncAfter;
}
