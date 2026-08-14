package com.moli.user.center.common.domain.dto.operation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class OperationFileUploadRequest {

    @NotNull(message = "serverId 不能为空")
    @ApiModelProperty(value = "目标服务器 ID", required = true)
    private Long serverId;

    @NotBlank(message = "targetPath 不能为空")
    @Size(max = 512, message = "targetPath 过长")
    @ApiModelProperty(value = "远程目标路径（绝对路径）", required = true)
    private String targetPath;

    @OperationPostAction
    @ApiModelProperty(value = "后置动作", example = "none")
    private String postAction = "none";

    @Size(max = 8192, message = "postCommand 过长")
    @ApiModelProperty("postAction=custom 时的 shell 命令")
    private String postCommand;
}
