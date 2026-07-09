package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OperationDeployStatusVo {

    private String serviceKey;
    private String action;

    @ApiModelProperty("脚本是否可调用")
    private Boolean available;

    @ApiModelProperty("解析为运行中")
    private Boolean running;

    private String output;
    private String message;
}
