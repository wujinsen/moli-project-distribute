package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("wiki↔DB 漂移项")
public class KbDriftItemVo {

    @ApiModelProperty("全路径 slug")
    private String slug;

    @ApiModelProperty("标题（DB 有则取 DB）")
    private String title;

    @ApiModelProperty("wiki 侧 hash")
    private String wikiHash;

    @ApiModelProperty("DB 侧 hash")
    private String dbHash;

    @ApiModelProperty("说明")
    private String detail;
}
