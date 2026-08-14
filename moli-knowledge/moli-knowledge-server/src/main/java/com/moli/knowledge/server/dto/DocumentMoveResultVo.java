package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("文档移动分类结果")
public class DocumentMoveResultVo {

    @ApiModelProperty("文档ID")
    private Long docId;

    @ApiModelProperty("移动前 slug")
    private String fromSlug;

    @ApiModelProperty("移动后 slug")
    private String toSlug;

    @ApiModelProperty("目标分类ID")
    private Long categoryId;

    @ApiModelProperty("随后触发的 Sync 是否成功")
    private Boolean syncSuccess;

    @ApiModelProperty("Sync 输出尾部（排错用）")
    private String syncOutputTail;
}
