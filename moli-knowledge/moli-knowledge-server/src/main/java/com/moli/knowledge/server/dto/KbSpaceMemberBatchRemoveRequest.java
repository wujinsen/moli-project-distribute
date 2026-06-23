package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("批量移除空间成员")
public class KbSpaceMemberBatchRemoveRequest {

    @ApiModelProperty(value = "kb_space_member 主键 ID 列表", required = true)
    private List<Long> ids;
}
