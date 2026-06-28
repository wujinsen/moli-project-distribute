package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("Wiki 治理 · 脚本修复请求")
public class WikiGovernScriptFixRequest {

    @ApiModelProperty(value = "空间 ID", required = true)
    private Long spaceId;

    @ApiModelProperty(value = "Lint 勾选的问题（仅需 page + kind）", required = true)
    private List<WikiLintIssueVo> issues;

    @ApiModelProperty("true=只预览不写盘")
    private Boolean dryRun;
}
