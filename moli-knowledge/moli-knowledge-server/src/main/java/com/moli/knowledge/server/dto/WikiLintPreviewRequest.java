package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Wiki 保存前 lint 预检请求")
public class WikiLintPreviewRequest {

    @ApiModelProperty(value = "slug", required = true)
    private String slug;

    @ApiModelProperty("所属空间ID")
    private Long spaceId;

    @ApiModelProperty(value = "待检全文", required = true)
    private String content;
}
