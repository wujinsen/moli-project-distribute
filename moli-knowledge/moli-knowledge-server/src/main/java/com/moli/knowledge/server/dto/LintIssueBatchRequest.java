package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 体检工单统一批量更新（O7/O8 · meiling-ui {@code PUT /kb/lint/issues/batch}）。
 */
@Data
@ApiModel("体检问题批量更新（状态 / 指派 / 优先级）")
public class LintIssueBatchRequest {

    @ApiModelProperty("问题 ID 列表")
    private List<Long> ids = new ArrayList<>();

    @ApiModelProperty("目标状态 0待处理/1已忽略/2已修复")
    private Integer status;

    @ApiModelProperty("处理人用户 ID；传 null 且 clearAssignee=true 时清空")
    private Long assigneeId;

    @ApiModelProperty("true=清空 assigneeId")
    private Boolean clearAssignee;

    @ApiModelProperty("0普通/1高/2紧急")
    private Integer priority;
}
