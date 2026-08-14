package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("Raw 上传跳过项")
public class RawUploadSkippedVo {

    @ApiModelProperty("相对 kb/raw/ 的路径")
    private String path;

    @ApiModelProperty("跳过原因，如 ALREADY_EXISTS")
    private String reason;
}
