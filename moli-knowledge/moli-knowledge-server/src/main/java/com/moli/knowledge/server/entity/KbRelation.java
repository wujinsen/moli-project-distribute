package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_relation")
@ApiModel("文档关系/图谱边")
public class KbRelation extends BaseEntity {

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("源文档ID")
    private Long sourceDocId;

    @ApiModelProperty("目标文档ID（断链时为空）")
    private Long targetDocId;

    @ApiModelProperty("目标标题（断链时保留原始[[标题]]）")
    private String targetTitle;

    @ApiModelProperty("关系类型 links_to/same_tag/related/supersedes/references")
    private String relationType;

    @ApiModelProperty("1已解析 0断链")
    private Integer resolved;

    @ApiModelProperty("权重")
    private Integer weight;

    @ApiModelProperty("0未删除 1已删除")
    private Integer isDelete;
}
