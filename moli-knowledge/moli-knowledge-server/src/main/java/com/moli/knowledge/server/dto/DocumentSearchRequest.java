package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("文档搜索请求")
public class DocumentSearchRequest {

    @ApiModelProperty("空间ID")
    private Long spaceId;

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
