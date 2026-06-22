package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_attachment")
@ApiModel("文档附件")
public class KbAttachment extends BaseEntity {

    @ApiModelProperty("文档ID")
    private Long documentId;

    @ApiModelProperty("原始文件名")
    private String fileName;

    @ApiModelProperty("MinIO 对象键")
    private String objectKey;

    @ApiModelProperty("文件大小(字节)")
    private Long fileSize;

    @ApiModelProperty("MIME 类型")
    private String contentType;

    @ApiModelProperty("0未删除 1已删除")
    private Integer isDelete;
}
