package com.moli.user.center.common.domain.dto.operation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class OperationDeployTaskRequest {

    @ApiModelProperty("目标服务器 ID（空则本机执行，需 allow-local）")
    private Long serverId;

    @ApiModelProperty("关联项目 ID（从项目台账跳转时传入；与 serviceKey 须一致）")
    private Long projectId;

    @NotBlank(message = "serviceKey 不能为空")
    @OperationDeployServiceKey
    @ApiModelProperty(value = "服务标识", required = true, example = "user-center")
    private String serviceKey;

    @NotBlank(message = "action 不能为空")
    @OperationDeployTaskAction
    @ApiModelProperty(value = "启停动作", required = true, example = "restart")
    private String action;
}
