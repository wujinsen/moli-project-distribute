package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Ingest 批次详情（含最新 plan）。
 */
@Data
@ApiModel("Ingest 批次详情")
public class IngestJobVo {

    @ApiModelProperty("批次任务ID")
    private Long id;

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("空间编码")
    private String spaceCode;

    @ApiModelProperty("批次号")
    private String batchNo;

    @ApiModelProperty("主题")
    private String topic;

    @ApiModelProperty("期望类型")
    private String expectTypes;

    @ApiModelProperty("勾选的 raw 路径")
    private List<String> rawPaths;

    @ApiModelProperty("状态")
    private String status;

    @ApiModelProperty("当前 plan 版本（0=未规划）")
    private Integer planVersion;

    @ApiModelProperty("最新 plan JSON（未规划时为空）")
    private String planJson;

    @ApiModelProperty("plan 来源 llm/manual")
    private String planSource;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("当前用户是否可编辑（editor）")
    private Boolean canEdit;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("修改时间")
    private Date updateTime;
}
