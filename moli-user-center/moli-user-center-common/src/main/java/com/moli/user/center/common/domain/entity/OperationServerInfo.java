package com.moli.user.center.common.domain.entity;

import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class OperationServerInfo extends BaseEntity {

    @ApiModelProperty("服务器名")
    private String serverName;

    @ApiModelProperty("ip")
    private String ip;

    @ApiModelProperty("内网ip")
    private String innerIp;

    @ApiModelProperty("端口号")
    private String port;

    @ApiModelProperty("开发环境: 1: dev 2:test 3:pre 4:pro")
    private Integer environment;

    private String remark;

    @ApiModelProperty("健康状态 0未知 1可达 2不可达 3跳过")
    private Integer status;

    @ApiModelProperty("最近探测时间")
    private java.util.Date lastCheckTime;
}
