package com.moli.knowledge.server.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 人工编辑 Plan 请求（PUT）。planJson 必须是合法 JSON 对象。
 */
@Data
@ApiModel("编辑 Plan")
public class IngestPlanUpdateRequest {

    @ApiModelProperty(value = "Plan JSON", required = true)
    @NotBlank(message = "planJson 不能为空")
    private String planJson;
}
