package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Ingest 批次的 Plan 版本（T15）。每次 LLM 生成或人工编辑都 append 一个新版本，便于回溯。
 * planJson 形态见 {@code kb/wiki/guides/Ingest工作台产品方案.md} §3.1（create/enrich/skip/edges/conflicts）。
 */
@Data
@TableName("kb_ingest_plan")
@ApiModel("Ingest 批次规划版本")
public class KbIngestPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("所属批次任务ID")
    private Long jobId;

    @ApiModelProperty("版本号（从 1 递增）")
    private Integer version;

    @ApiModelProperty("Plan JSON（create/enrich/skip/edges/conflicts）")
    private String planJson;

    @ApiModelProperty("来源：llm / manual")
    private String source;

    @ApiModelProperty("LLM 提供方（source=llm 时）")
    private String provider;

    @ApiModelProperty("模型名（source=llm 时）")
    private String model;

    @ApiModelProperty("创建人")
    private Long createId;

    @ApiModelProperty("创建时间")
    private Date createTime;
}
