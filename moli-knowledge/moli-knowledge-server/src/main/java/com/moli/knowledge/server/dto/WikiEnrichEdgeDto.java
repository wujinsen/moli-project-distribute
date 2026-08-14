package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Wiki enrich 关系边")
public class WikiEnrichEdgeDto {

    @ApiModelProperty("源 slug（全路径或裸名）")
    private String from;

    @ApiModelProperty("目标 slug")
    private String to;

    @ApiModelProperty("depends_on|relates_to|derived_from|supersedes|part_of")
    private String type;

    @ApiModelProperty("证据说明")
    private String evidence;
}
