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

    @ApiModelProperty("是否正在同步（Redis 锁）")
    private boolean running;

    @ApiModelProperty("空间编码")
    private String spaceCode;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("最近一批批次号（O1 别名）")
    private String lastBatchNo;

    @ApiModelProperty("最近一批状态：success / fail / running")
    private String lastStatus;

    @ApiModelProperty("最近一批摘要或错误信息")
    private String lastMessage;

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("最近同步时间")
    private Date lastSyncTime;

    @ApiModelProperty("最近一批完成时间（O1 别名）")
    private Date lastFinishTime;

    @ApiModelProperty("本批次文档动作总数（不含 batch 汇总行）")
    private int total;

    @ApiModelProperty("按 action 计数：insert/update/delete/skip")
    private Map<String, Integer> actionCounts = new LinkedHashMap<>();

    @ApiModelProperty("失败条数")
    private int failCount;

    @ApiModelProperty("成功条数（文档级 status=success）")
    private int successCount;
}
