package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@ApiModel("DeepResearch 启动响应")
public class ResearchStartVo {

    @ApiModelProperty("运行 ID")
    private String runId;

    @ApiModelProperty("PENDING|RUNNING")
    private String status;
}
