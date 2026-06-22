package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_comment")
@ApiModel("文档评论")
public class KbComment extends BaseEntity {

    @ApiModelProperty("文档ID")
    private Long documentId;

    @ApiModelProperty("父评论ID，0为根")
    private Long parentId;

    @ApiModelProperty("评论内容")
    private String content;

    @ApiModelProperty("0未删除 1已删除")
    private Integer isDelete;
}
