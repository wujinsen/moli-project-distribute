package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 任务轮询结果（SVR-14）。前端按 logOffset 拉增量日志。
 */
@Data
public class OperationTaskVo {

    private Long id;
    private String taskType;
    private Long serverId;
    private String serviceKey;
    private String action;
    private String targetName;

    @ApiModelProperty("pending / running / success / failed")
    private String status;

    @ApiModelProperty("进度 0-100")
    private Integer progress;

    @ApiModelProperty("结果摘要 / 失败原因")
    private String message;

    @ApiModelProperty("本次返回的增量日志片段")
    private String logChunk;

    @ApiModelProperty("下次轮询应传入的 logOffset")
    private Integer nextLogOffset;

    @ApiModelProperty("任务是否已结束（success/failed）")
    private Boolean finished;

    private Date createTime;
    private Date finishTime;
}
