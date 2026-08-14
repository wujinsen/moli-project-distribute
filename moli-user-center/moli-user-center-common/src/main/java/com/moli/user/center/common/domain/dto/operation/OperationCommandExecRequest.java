package com.moli.user.center.common.domain.dto.operation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class OperationCommandExecRequest {

    @NotNull(message = "serverId 不能为空")
    @ApiModelProperty(value = "目标服务器 ID", required = true)
    private Long serverId;

    @NotBlank(message = "command 不能为空")
    @Size(max = 8192, message = "command 过长")
    @ApiModelProperty(value = "shell 命令", required = true)
    private String command;

    @Size(max = 512, message = "workDir 过长")
    @ApiModelProperty("可选工作目录（绝对路径）")
    private String workDir;
}
