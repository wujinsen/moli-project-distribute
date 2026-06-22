package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("文档搜索请求")
public class DocumentSearchRequest {

    @ApiModelProperty("空间ID（与 spaceIds 同时传时以 spaceIds 为准）")
    private Long spaceId;

    @ApiModelProperty("多空间ID")
    private List<Long> spaceIds;

    @ApiModelProperty("分类ID")
    private Long categoryId;

    @ApiModelProperty("关键词")
    private String keyword;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("标签ID")
    private Long tagId;

    @ApiModelProperty("页码")
    private Integer pageNum = 1;

    @ApiModelProperty("每页条数")
    private Integer pageSize = 10;
}
