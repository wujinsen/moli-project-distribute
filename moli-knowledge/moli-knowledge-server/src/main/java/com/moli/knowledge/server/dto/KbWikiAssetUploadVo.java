package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * T22 F2：Wiki 页 inline 插图上传结果。
 */
@Data
@ApiModel("Wiki inline 插图上传结果")
public class KbWikiAssetUploadVo {

    @ApiModelProperty("相对 markdown 的路径，如 assets/img-123.png")
    private String rel;

    @ApiModelProperty("磁盘文件名")
    private String fileName;

    @ApiModelProperty("字节大小")
    private long fileSize;

    @ApiModelProperty("MIME 类型")
    private String contentType;

    @ApiModelProperty("建议插入 markdown 片段")
    private String markdown;
}
