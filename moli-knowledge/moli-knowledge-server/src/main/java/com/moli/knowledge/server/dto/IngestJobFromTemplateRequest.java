package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("从模板创建批次")
public class IngestJobFromTemplateRequest {

    @ApiModelProperty("覆盖批次号（空则自动生成）")
    private String batchNo;

    @ApiModelProperty("覆盖主题（空则用模板主题）")
    private String topic;
}
