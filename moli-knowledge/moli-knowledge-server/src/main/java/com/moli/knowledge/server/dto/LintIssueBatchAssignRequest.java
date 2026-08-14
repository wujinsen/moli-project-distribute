package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("体检问题批量指派")
public class LintIssueBatchAssignRequest {

    @ApiModelProperty("问题 ID 列表")
    private List<Long> ids = new ArrayList<>();

    @ApiModelProperty("处理人用户 ID")
    private Long assigneeId;

    @ApiModelProperty("0普通/1高/2紧急（可空表示不改）")
    private Integer priority;
}
