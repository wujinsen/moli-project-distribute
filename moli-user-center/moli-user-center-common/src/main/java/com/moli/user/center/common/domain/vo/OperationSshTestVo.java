package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * SSH 连接测试结果（SVR-13）。
 */
@Data
public class OperationSshTestVo {

    @ApiModelProperty("是否连接成功")
    private Boolean success;

    @ApiModelProperty("实际连接使用的主机（内网/公网）")
    private String host;

    @ApiModelProperty("whoami 输出等诊断信息")
    private String output;

    @ApiModelProperty("连接耗时（毫秒）")
    private Long elapsedMs;

    @ApiModelProperty("失败原因")
    private String message;
}
