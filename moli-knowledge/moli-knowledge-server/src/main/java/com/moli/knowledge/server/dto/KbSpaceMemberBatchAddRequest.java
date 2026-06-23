package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("批量添加空间成员")
public class KbSpaceMemberBatchAddRequest {

    @ApiModelProperty(value = "空间ID", required = true)
    private Long spaceId;

    @ApiModelProperty("成员类型 0用户/1角色，默认 0")
    private Integer memberType;

    @ApiModelProperty(value = "用户ID或角色ID列表", required = true)
    private List<Long> memberIds;

    @ApiModelProperty("角色 viewer/editor/admin，默认 viewer")
    private String role;
}
