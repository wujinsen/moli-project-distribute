package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OperationComponentVo {

    private Long id;
    private Long createId;
    private Date createTime;
    private Long updateId;
    private Date updateTime;

    @ApiModelProperty("组件名")
    private String componentName;
    @ApiModelProperty("主服务器 ID")
    private Long serverId;
    /** N:N 关联的服务器 ID（含主 serverId） */
    private List<Long> serverIds;
    @ApiModelProperty("服务器ip")
    private String serverIp;
    @ApiModelProperty("账户名")
    private String account;
    @ApiModelProperty("部署路径")
    private String deployPath;
    @ApiModelProperty("端口号")
    private String port;
    @ApiModelProperty("版本号")
    private String version;
    @ApiModelProperty("开发环境: 1: dev 2:test 3:pre 4:pro")
    private Integer environment;
    private String remark;

    @ApiModelProperty("是否已配置密码")
    private Boolean passwordConfigured;
    @ApiModelProperty("密码掩码（末四位）")
    private String passwordMask;

    @ApiModelProperty("健康状态 0未知 1可达 2不可达 3跳过")
    private Integer status;
    private Date lastCheckTime;

    @ApiModelProperty("矩阵期望端口（可映射时）")
    private String expectedPort;
    @ApiModelProperty("0未映射 1一致 2不符 3跳过")
    private Integer portMatchStatus;

    @ApiModelProperty("关联服务器数量（含主 server_id 与 N:N）")
    private Integer serverCount;

    @ApiModelProperty("被项目依赖数量")
    private Integer projectCount;
}
