package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Ingest 批次任务（T15）。一行 = 一次「raw → 多页 wiki」批次的生命周期记录。
 * 草稿/落盘明细在 T15b/c 的 kb_ingest_draft / kb_ingest_commit；本表只管批次元数据 + 状态机。
 */
@Data
@TableName("kb_ingest_job")
@ApiModel("Ingest 批次任务")
public class KbIngestJob implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("目标空间ID")
    private Long spaceId;

    @ApiModelProperty("批次号（与 log.md 一致，可空由系统生成）")
    private String batchNo;

    @ApiModelProperty("主题（如 Redis 哨兵）")
    private String topic;

    @ApiModelProperty("期望产出类型 article/guide/service/concept... 逗号分隔，可空")
    private String expectTypes;

    @ApiModelProperty("勾选的 raw 路径 JSON 数组（相对 rawRoot）")
    private String rawPaths;

    @ApiModelProperty("状态：created/planned/generating/reviewing/committed/cancelled")
    private String status;

    @ApiModelProperty("当前 plan 版本号（0=未规划）")
    private Integer planVersion;

    @ApiModelProperty("备注/最近一次说明")
    private String remark;

    @ApiModelProperty("创建人")
    private Long createId;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("修改人")
    private Long updateId;

    @ApiModelProperty("修改时间")
    private Date updateTime;

    @ApiModelProperty("0未删除 1已删除")
    private Integer isDelete;
}
