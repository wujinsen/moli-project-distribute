package com.moli.knowledge.server.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Ingest 批次模板（T15e）。保存 raw 范围、主题、期望类型，可选 Plan 快照，用于快速新建批次。
 */
@Data
@TableName("kb_ingest_template")
@ApiModel("Ingest 批次模板")
public class KbIngestTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("目标空间ID")
    private Long spaceId;

    @ApiModelProperty("模板名称")
    private String name;

    @ApiModelProperty("默认主题")
    private String topic;

    @ApiModelProperty("期望产出类型，逗号分隔")
    private String expectTypes;

    @ApiModelProperty("raw 路径 JSON 数组")
    private String rawPaths;

    @ApiModelProperty("可选 Plan JSON 快照")
    private String planJson;

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
