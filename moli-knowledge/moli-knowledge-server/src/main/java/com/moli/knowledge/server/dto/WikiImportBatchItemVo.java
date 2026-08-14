package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Wiki 批量导入单项 meta（T20c）")
public class WikiImportBatchItemVo {

    @ApiModelProperty("files 数组下标（0-based）")
    private Integer fileIndex;

    @ApiModelProperty("分类 ID；缺省用请求级 categoryId")
    private Long categoryId;

    @ApiModelProperty("裸 slug")
    private String slug;

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("FAIL/OVERWRITE；缺省用请求级 onConflict")
    private String onConflict;
}
