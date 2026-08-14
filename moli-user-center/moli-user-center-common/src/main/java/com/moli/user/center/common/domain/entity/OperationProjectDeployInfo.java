package com.moli.user.center.common.domain.entity;

import com.moli.common.core.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OperationProjectDeployInfo extends BaseEntity {
    @ApiModelProperty("服务器ID")
    private Long serverId;
    @ApiModelProperty("服务器 IP")
    private String serverIp;
    @ApiModelProperty("内网 IP")
    private String innerIp;
    @ApiModelProperty("访问 URL")
    private String url;
    @ApiModelProperty("项目名称")
    private String projectName;
    @ApiModelProperty("部署路径")
    private String deployPath;
    @ApiModelProperty("端口")
    private String port;
    @ApiModelProperty("开发环境: 1: dev 2:test 3:pre 4:pro")
    private Integer environment;
    private String remark;

    @ApiModelProperty("部署进程是否运行（定时同步）")
    private Boolean deployRunning;
    private java.util.Date lastDeployCheckTime;

    @TableField(exist = false)
    @ApiModelProperty("按依赖组件筛选")
    private Long componentId;
}
