package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 任务历史按项目分组（DC-4）。
 */
@Data
public class OperationTaskProjectGroupVo {

    @ApiModelProperty("项目 ID；null 表示未关联项目")
    private Long projectId;

    @ApiModelProperty("项目名称（来自 operation_project_deploy_info）")
    private String projectName;

    @ApiModelProperty("组内任务总数")
    private Integer taskCount;

    @ApiModelProperty("进行中任务数（pending + running）")
    private Integer runningCount;

    @ApiModelProperty("失败任务数")
    private Integer failedCount;

    @ApiModelProperty("成功任务数")
    private Integer successCount;

    @ApiModelProperty("组内最近任务创建时间")
    private Date latestCreateTime;

    @ApiModelProperty("组内最近任务（createTime 降序，条数受 tasksPerGroup 限制）")
    private List<OperationTaskVo> tasks;
}
