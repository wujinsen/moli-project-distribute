package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_document_chunk")
@ApiModel("知识文档切段")
public class KbDocumentChunk extends BaseEntity {

    @ApiModelProperty("kb_document.id")
    private Long documentId;

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("slug")
    private String slug;

    @ApiModelProperty("体裁")
    private String kbType;

    @ApiModelProperty("分类ID")
    private Long categoryId;

    @ApiModelProperty("状态 0草稿 1已发布 2已归档")
    private Integer status;

    @ApiModelProperty("页内顺序 0-based")
    private Integer chunkIndex;

    @ApiModelProperty("节标题")
    private String heading;

    @ApiModelProperty("0页首 2=## 3=###")
    private Integer headingLevel;

    @ApiModelProperty("切段正文")
    private String content;

    @ApiModelProperty("字符数")
    private Integer charCount;

    @ApiModelProperty("SHA-256")
    private String contentHash;
}
