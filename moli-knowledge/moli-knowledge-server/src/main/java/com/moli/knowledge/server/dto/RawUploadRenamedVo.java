package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Raw 上传重命名项")
public class RawUploadRenamedVo {

    @ApiModelProperty("最终相对路径")
    private String path;

    @ApiModelProperty("原始文件名")
    private String originalName;
}
