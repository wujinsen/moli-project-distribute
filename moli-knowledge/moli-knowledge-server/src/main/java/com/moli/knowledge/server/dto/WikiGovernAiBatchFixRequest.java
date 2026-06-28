package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("Wiki 治理 · AI 批量修复请求")
public class WikiGovernAiBatchFixRequest {

    @ApiModelProperty(value = "空间 ID", required = true)
    private Long spaceId;

    @ApiModelProperty(value = "Lint 勾选的问题", required = true)
    private List<WikiLintIssueVo> issues;

    @ApiModelProperty("模型 ID；默认 kb.llm.model")
    private String model;

    @ApiModelProperty("true=只生成建议不写盘")
    private Boolean dryRun;
}
