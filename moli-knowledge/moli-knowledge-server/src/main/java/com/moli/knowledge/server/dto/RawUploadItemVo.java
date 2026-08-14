package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Raw 上传成功项")
public class RawUploadItemVo {

    @ApiModelProperty("相对 kb/raw/ 的路径")
    private String path;

    @ApiModelProperty("字节大小")
    private long size;

    @ApiModelProperty("是否覆盖已有文件")
    private boolean overwritten;
}
