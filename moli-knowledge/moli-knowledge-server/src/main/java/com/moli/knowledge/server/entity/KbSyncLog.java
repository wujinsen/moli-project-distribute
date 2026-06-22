package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("kb_sync_log")
@ApiModel("知识同步日志")
public class KbSyncLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("文档ID")
    private Long documentId;

    @ApiModelProperty("kb/ 原始路径")
    private String sourcePath;

    @ApiModelProperty("insert/update/delete/skip")
    private String action;

    @ApiModelProperty("同步时内容 SHA-256")
    private String contentHash;

    @ApiModelProperty("success/fail")
    private String status;

    @ApiModelProperty("说明/错误信息")
    private String message;

    @ApiModelProperty("创建时间")
    private Date createTime;
}
