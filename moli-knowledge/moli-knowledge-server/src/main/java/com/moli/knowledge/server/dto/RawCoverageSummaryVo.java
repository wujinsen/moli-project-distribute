package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("raw 覆盖统计")
public class RawCoverageSummaryVo {

    @ApiModelProperty("扫描到的 raw 文件总数")
    private int totalFiles;

    @ApiModelProperty("covered 文件数")
    private int covered;

    @ApiModelProperty("cluster 文件数")
    private int cluster;

    @ApiModelProperty("open 文件数")
    private int open;
}
