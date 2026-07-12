package com.moli.user.center.common.domain.entity;

import com.moli.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OperationComponentDeployInfo extends BaseEntity {

    @ApiModelProperty("组件名")
    private String componentName;
    @ApiModelProperty("服务器ID")
    private Long serverId;
    @ApiModelProperty("服务器ip")
    private String serverIp;
    @ApiModelProperty("账户名")
    private String account;
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("部署路径")
    private String deployPath;
    @ApiModelProperty("端口号")
    private String port;
    @ApiModelProperty("版本号")
    private String version;
    @ApiModelProperty("开发环境: 1: dev 2:test 3:pre 4:pro")
    private Integer environment;
    private String remark;

    @ApiModelProperty("健康状态 0未知 1可达 2不可达 3跳过")
    private Integer status;

    @ApiModelProperty("最近探测时间")
    private java.util.Date lastCheckTime;

    @TableField(exist = false)
    @ApiModelProperty("按依赖项目筛选")
    private Long projectId;
}
