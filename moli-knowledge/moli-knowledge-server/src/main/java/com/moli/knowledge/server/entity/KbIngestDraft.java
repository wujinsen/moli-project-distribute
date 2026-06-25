package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Ingest 批次的单页草稿（T15b）。一行=plan 里一个 create/enrich 目标页。
 * 草稿在 commit 前只存 DB，不写 wiki 文件。
 */
@Data
@TableName("kb_ingest_draft")
@ApiModel("Ingest 批次每页草稿")
public class KbIngestDraft implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("所属批次任务ID")
    private Long jobId;

    @ApiModelProperty("目标 slug（相对 wiki 目录的完整相对路径，如 articles/redis-哨兵部署）")
    private String slug;

    @ApiModelProperty("知识类型 article/guide/service/concept/interview/output")
    private String kbType;

    @ApiModelProperty("create / enrich")
    private String action;

    @ApiModelProperty("enrich 基线（当前 wiki 全文）；create 为空")
    private String baseline;

    @ApiModelProperty("enrich 追加段落（patch）；create 为空")
    private String patch;

    @ApiModelProperty("草稿正文（create=全文；enrich=baseline+patch 合并预览，落盘用）")
    private String draft;

    @ApiModelProperty("draft / approved / rejected")
    private String approval;

    @ApiModelProperty("创建人")
    private Long createId;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("修改时间")
    private Date updateTime;
}
