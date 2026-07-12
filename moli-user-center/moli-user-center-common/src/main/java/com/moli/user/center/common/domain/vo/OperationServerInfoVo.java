package com.moli.user.center.common.domain.vo;

import com.moli.common.core.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class OperationServerInfoVo extends BaseEntity {

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

    @ApiModelProperty("角色 app/db/cache/mq/gateway/bastion/middleware/other")
    private String serverRole;

    @ApiModelProperty("按标签筛选（精确匹配单项）")
    private String tag;

    private String remark;
}
