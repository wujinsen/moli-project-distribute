package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Ingest 批次落盘记录（T15c）。一行=一次 commit（写 wiki + log + index + edges）。
 */
@Data
@TableName("kb_ingest_commit")
@ApiModel("Ingest 批次落盘记录")
public class KbIngestCommit implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("所属批次任务ID")
    private Long jobId;

    @ApiModelProperty("写入文件列表 JSON")
    private String filesJson;

    @ApiModelProperty("关联 Sync 批次号")
    private String syncBatchNo;

    @ApiModelProperty("创建人")
    private Long createId;

    @ApiModelProperty("落盘时间")
    private Date createTime;
}
