package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("同步触发结果")
public class SyncTriggerVo {

    @ApiModelProperty("空间 ID")
    private Long spaceId;

    @ApiModelProperty("是否成功")
    private boolean success;

    @ApiModelProperty("进程退出码")
    private int exitCode;

    @ApiModelProperty("space_code")
    private String spaceCode;

    @ApiModelProperty("批次号（脚本 stdout 解析）")
    private String batchNo;

    @ApiModelProperty("success / fail")
    private String status;

    @ApiModelProperty("摘要或错误信息")
    private String message;

    @ApiModelProperty("脚本标准输出摘要（末尾）")
    private String outputTail;

    @ApiModelProperty("Sync 成功后建议下一步")
    private List<KbWorkflowHintVo> nextSteps;
}
