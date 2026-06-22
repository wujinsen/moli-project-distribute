package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("当前用户可读的知识空间（含权限摘要）")
public class KbAccessibleSpaceVo {

    @ApiModelProperty("空间ID")
    private Long id;

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

    @ApiModelProperty("当前用户是否可编辑内容")
    private Boolean canEdit;

    @ApiModelProperty("当前用户是否可管理空间")
    private Boolean canAdmin;
}
