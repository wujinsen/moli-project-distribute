package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Ingest 单页草稿（含 baseline ↔ draft，供前端 diff）。
 */
@Data
@ApiModel("Ingest 单页草稿")
public class IngestDraftVo {

    @ApiModelProperty("草稿ID")
    private Long id;

    @ApiModelProperty("批次任务ID")
    private Long jobId;

    @ApiModelProperty("目标 slug（相对 wiki 目录的完整路径）")
    private String slug;

    @ApiModelProperty("展示用 slug（末段，[[]] 引用名）")
    private String displaySlug;

    @ApiModelProperty("知识类型")
    private String kbType;

    @ApiModelProperty("create / enrich")
    private String action;

    @ApiModelProperty("enrich 基线（create 为空）")
    private String baseline;

    @ApiModelProperty("enrich 追加段落 patch（create 为空）")
    private String patch;

    @ApiModelProperty("合并预览/落盘全文（enrich=baseline+patch；create=全文）")
    private String draft;

    @ApiModelProperty("draft / approved / rejected")
    private String approval;

    @ApiModelProperty("修改时间")
    private Date updateTime;
}
