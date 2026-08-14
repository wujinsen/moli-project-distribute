package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("空间级文件 Lint 请求（文件真值，调 lint.py）")
public class WikiSpaceLintRequest {

    @ApiModelProperty("空间 ID（与 spaceCode 二选一；省略=默认 enterprise-kb）")
    private Long spaceId;

    @ApiModelProperty("空间编码（与 spaceId 二选一）")
    private String spaceCode;

    @ApiModelProperty("严格模式：WARN 也计入失败（仅影响 exitCode，不影响返回的问题清单）")
    private Boolean strict;
}
