package com.moli.user.center.common.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 运维异步任务（SVR-14）：部署启停 / 文件上传 / 命令执行的执行记录与进度。
 */
@Data
@TableName("operation_task")
public class OperationTask extends BaseEntity {

    @ApiModelProperty("任务类型：deploy / upload / command / health_probe")
    private String taskType;

    @ApiModelProperty("目标服务器ID（本机执行时可空）")
    private Long serverId;

    @ApiModelProperty("关联项目 ID（deploy 从项目页发起时）")
    private Long projectId;

    @ApiModelProperty("服务标识：user-center / gateway / knowledge")
    private String serviceKey;

    @ApiModelProperty("动作：start / stop / restart / upload 等")
    private String action;

    @ApiModelProperty("目标描述（jar 名 / 目标路径等）")
    private String targetName;

    @ApiModelProperty("状态：pending / running / success / failed")
    private String status;

    @ApiModelProperty("进度 0-100")
    private Integer progress;

    @ApiModelProperty("执行日志（累加）")
    private String taskLog;

    @ApiModelProperty("结果摘要 / 失败原因")
    private String message;

    @ApiModelProperty("完成时间")
    private Date finishTime;
}
