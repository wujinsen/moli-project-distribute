package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * T15f · 异步启动 Ingest 多页生成。
 */
@Data
@ApiModel("Ingest 异步生成启动结果")
public class IngestGenerateStartVo {

    @ApiModelProperty("SSE 订阅 taskId")
    private String taskId;

    @ApiModelProperty("批次 jobId")
    private Long jobId;

    @ApiModelProperty("Plan 应生成总页数")
    private int total;

    @ApiModelProperty("是否续跑")
    private boolean resume;

    @ApiModelProperty("是否模板模式")
    private boolean templateMode;

    @ApiModelProperty("任务状态：running")
    private String status;
}
