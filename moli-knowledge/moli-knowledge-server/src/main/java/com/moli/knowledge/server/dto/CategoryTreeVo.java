package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("分类树节点")
public class CategoryTreeVo {

    @ApiModelProperty("分类ID")
    private Long id;

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("父分类ID")
    private Long parentId;

    @ApiModelProperty("分类名称")
    private String categoryName;

    @ApiModelProperty("图标")
    private String icon;

    @ApiModelProperty("绑定的 wiki 子目录名")
    private String dirSlug;

    @ApiModelProperty("默认体裁 kb_type")
    private String defaultType;

    @ApiModelProperty("该分类下文档数（withCount=true 时返回，否则 null）")
    private Integer docCount;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("子分类")
    private List<CategoryTreeVo> children;
}
