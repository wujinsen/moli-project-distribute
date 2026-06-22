package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_tag")
@ApiModel("知识标签")
public class KbTag extends BaseEntity {

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("标签名")
    private String tagName;

    @ApiModelProperty("颜色")
    private String color;

    @ApiModelProperty("0未删除 1已删除")
    private Integer isDelete;
}
