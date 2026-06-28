package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("Wiki 治理 · 重复页合并提示请求")
public class WikiGovernMergeHintRequest {

    @ApiModelProperty(value = "空间 ID", required = true)
    private Long spaceId;

    @ApiModelProperty(value = "Lint 勾选的 dup_slug / dup_content / near_dup", required = true)
    private List<WikiLintIssueVo> issues;
}
