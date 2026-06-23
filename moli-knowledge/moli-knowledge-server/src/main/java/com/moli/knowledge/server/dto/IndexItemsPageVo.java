package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("目录分组条目分页")
public class IndexItemsPageVo {

    @ApiModelProperty("类型")
    private String type;

    @ApiModelProperty("类型中文名")
    private String label;

    @ApiModelProperty("该类型总数")
    private long total;

    @ApiModelProperty("页码")
    private int pageNum;

    @ApiModelProperty("每页条数")
    private int pageSize;

    @ApiModelProperty("当前页条目（轻量：无 summary）")
    private List<IndexTreeVo.Item> items = new ArrayList<>();
}
