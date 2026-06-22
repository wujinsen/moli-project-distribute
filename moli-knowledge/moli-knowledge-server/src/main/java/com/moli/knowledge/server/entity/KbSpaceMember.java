package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("kb_space_member")
@ApiModel("知识空间成员")
public class KbSpaceMember extends BaseEntity {

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("成员类型 0用户 1角色")
    private Integer memberType;

    @ApiModelProperty("用户ID或角色ID")
    private Long memberId;

    @ApiModelProperty("角色 viewer/editor/admin")
    private String role;

    @ApiModelProperty("0未删除 1已删除")
    private Integer isDelete;
}
