package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OperationServerVo {

    private Long id;
    private Long createId;
    private Date createTime;
    private Long updateId;
    private Date updateTime;

    private String serverName;
    private String ip;
    private String innerIp;
    private String port;
    private Integer environment;
    private String serverRole;
    private List<String> tags;
    private String remark;

    @ApiModelProperty("健康状态 0未知 1可达 2不可达 3跳过")
    private Integer status;
    private Date lastCheckTime;

    @ApiModelProperty("SSH 端口")
    private Integer sshPort;

    @ApiModelProperty("SSH 登录用户")
    private String sshUser;

    @ApiModelProperty("SSH 认证方式：1 私钥 2 密码")
    private Integer sshAuthType;

    @ApiModelProperty("连接偏好：auto/inner/public")
    private String connPref;

    @ApiModelProperty("是否已配置 SSH 凭据（私钥/密码不回显）")
    private Boolean sshConfigured;

    @ApiModelProperty("该服务器允许上传的路径前缀")
    private String uploadAllowedRoots;

    @ApiModelProperty("关联项目数量")
    private Integer projectCount;

    @ApiModelProperty("关联组件数量")
    private Integer componentCount;
}
