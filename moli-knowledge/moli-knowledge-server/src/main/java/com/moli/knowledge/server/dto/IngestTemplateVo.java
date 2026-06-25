package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("Ingest 批次模板")
public class IngestTemplateVo {

    @ApiModelProperty("模板ID")
    private Long id;

    @ApiModelProperty("空间ID")
    private Long spaceId;

    @ApiModelProperty("空间编码")
    private String spaceCode;

    @ApiModelProperty("模板名称")
    private String name;

    @ApiModelProperty("默认主题")
    private String topic;

    @ApiModelProperty("期望类型")
    private String expectTypes;

    @ApiModelProperty("raw 路径列表")
    private List<String> rawPaths;

    @ApiModelProperty("是否含 Plan 快照")
    private Boolean hasPlan;

    @ApiModelProperty("创建时间")
    private Date createTime;
}
