package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("批次另存为模板")
public class IngestSaveAsTemplateRequest {

    @ApiModelProperty(value = "模板名称", required = true)
    private String name;

    @ApiModelProperty("是否附带当前 Plan 快照（默认 true）")
    private Boolean includePlan;
}
