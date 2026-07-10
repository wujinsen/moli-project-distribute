package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Wiki 批量导入失败项")
public class WikiImportBatchFailVo {

    @ApiModelProperty("原始文件名")
    private String fileName;

    @ApiModelProperty("失败原因")
    private String reason;
}
