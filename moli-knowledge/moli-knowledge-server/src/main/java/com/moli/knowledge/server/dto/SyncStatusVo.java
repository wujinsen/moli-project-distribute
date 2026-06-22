package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@ApiModel("同步批次状态")
public class SyncStatusVo {

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("最近同步时间")
    private Date lastSyncTime;

    @ApiModelProperty("本批次动作总数")
    private int total;

    @ApiModelProperty("按 action 计数：insert/update/delete/skip")
    private Map<String, Integer> actionCounts = new LinkedHashMap<>();

    @ApiModelProperty("失败条数")
    private int failCount;
}
