package com.moli.knowledge.server.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("DB 体检 Scan 状态（只读；定时开关由运维 yml 配置）")
public class LintScanStatusVo {

    @ApiModelProperty("空间 ID")
    private Long spaceId;

    @ApiModelProperty("空间编码")
    private String spaceCode;

    @ApiModelProperty("是否启用定时 scan 落库（kb.lint.schedule-enabled，只读）")
    private boolean scheduleEnabled;

    @ApiModelProperty("定时 cron 表达式（kb.lint.schedule-cron，只读；未配置时返回默认）")
    private String scheduleCron;

    @ApiModelProperty("最近一次 scan 落库时间（手动或定时；无记录时为 null）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastScanTime;

    @ApiModelProperty("当前待处理工单数（status=0）")
    private int openIssueCount;
}
