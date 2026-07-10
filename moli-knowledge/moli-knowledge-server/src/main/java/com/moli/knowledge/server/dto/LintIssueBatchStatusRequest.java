package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("体检问题批量状态变更")
public class LintIssueBatchStatusRequest {

    @ApiModelProperty("问题 ID 列表")
    private List<Long> ids = new ArrayList<>();

    @ApiModelProperty("目标状态 0待处理/1已忽略/2已修复")
    private Integer status;
}
