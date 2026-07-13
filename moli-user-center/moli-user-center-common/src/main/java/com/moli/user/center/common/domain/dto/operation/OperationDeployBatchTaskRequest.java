package com.moli.user.center.common.domain.dto.operation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class OperationDeployBatchTaskRequest {

    @NotEmpty(message = "steps 不能为空")
    @Size(min = 1, max = 32, message = "steps 数量须在 1~32 之间")
    @Valid
    @ApiModelProperty(value = "滚动步骤（按序执行）", required = true)
    private List<OperationDeployTaskRequest> steps;

    @ApiModelProperty("批次级 projectId；步骤未传时回填")
    private Long projectId;

    @ApiModelProperty("某步失败时是否中断后续步骤，默认 true")
    private Boolean stopOnFailure = Boolean.TRUE;

    @Min(value = 0, message = "intervalSeconds 不能为负")
    @Max(value = 300, message = "intervalSeconds 不能超过 300")
    @ApiModelProperty("步骤间隔秒数（滚动重启），默认 0")
    private Integer intervalSeconds = 0;
}
