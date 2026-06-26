package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_category")
@ApiModel("知识分类")
public class KbCategory extends BaseEntity {

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("父分类ID，0为根")
    private Long parentId;

    @ApiModelProperty("分类名称")
    private String categoryName;

    @ApiModelProperty("图标")
    private String icon;

    @ApiModelProperty("绑定的 wiki 子目录名（分类=目录，单一真相源）")
    private String dirSlug;

    @ApiModelProperty("该分类默认体裁 kb_type；文档移入时按此改 frontmatter type")
    private String defaultType;

    @ApiModelProperty("排序")
    private Integer sort;

    @ApiModelProperty("0未删除 1已删除")
    private Integer isDelete;
}
