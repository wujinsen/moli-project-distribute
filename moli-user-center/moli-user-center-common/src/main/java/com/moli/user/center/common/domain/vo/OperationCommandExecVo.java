package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 远程命令执行请求（SVR-18）。
 */
@Data
public class OperationCommandExecVo {

    @ApiModelProperty(value = "目标服务器 ID", required = true)
    private Long serverId;

    @ApiModelProperty(value = "shell 命令（可多行）", required = true)
    private String command;

    @ApiModelProperty("可选工作目录（绝对路径）")
    private String workDir;
}
