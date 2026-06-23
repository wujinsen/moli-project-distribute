package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("目录 slug 定位")
public class IndexLocateVo {

    @ApiModelProperty("所属类型")
    private String type;

    @ApiModelProperty("类型中文名")
    private String label;

    @ApiModelProperty("文档条目")
    private IndexTreeVo.Item item;
}
