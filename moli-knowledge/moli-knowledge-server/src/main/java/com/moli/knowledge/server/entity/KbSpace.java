package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_space")
@ApiModel("知识空间")
public class KbSpace extends BaseEntity {

    @ApiModelProperty("空间编码")
    private String spaceCode;

    @ApiModelProperty("空间名称")
    private String spaceName;

    @ApiModelProperty("描述")
    private String description;

    @ApiModelProperty("图标")
    private String icon;

    @ApiModelProperty("可见性 0私有 1内部 2公开")
    private Integer visibility;

    @ApiModelProperty("负责人用户ID")
    private Long ownerId;

    @ApiModelProperty("1启用 0停用")
    private Integer status;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("0未删除 1已删除")
    private Integer isDelete;
}
