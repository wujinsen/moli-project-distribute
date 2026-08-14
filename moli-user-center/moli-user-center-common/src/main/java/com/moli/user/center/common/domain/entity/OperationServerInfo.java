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

    @ApiModelProperty("角色 app/db/cache/mq/gateway/bastion/middleware/other")
    private String serverRole;

    @ApiModelProperty("标签 JSON 数组")
    private String tags;

    private String remark;

    @ApiModelProperty("健康状态 0未知 1可达 2不可达 3跳过")
    private Integer status;

    @ApiModelProperty("最近探测时间")
    private java.util.Date lastCheckTime;

    @ApiModelProperty("SSH 端口，默认 22")
    private Integer sshPort;

    @ApiModelProperty("SSH 登录用户，默认 ubuntu")
    private String sshUser;

    @ApiModelProperty("SSH 认证方式：1 私钥 2 密码")
    private Integer sshAuthType;

    @ApiModelProperty("SSH 私钥（AES-GCM 密文，只写不读）")
    private String sshPrivateKey;

    @ApiModelProperty("SSH 私钥口令 / 登录密码（AES-GCM 密文，只写不读）")
    private String sshPassphrase;

    @ApiModelProperty("连接偏好：auto 内网优先 / inner 仅内网 / public 仅公网")
    private String connPref;

    @ApiModelProperty("该服务器允许上传的路径前缀，逗号或换行分隔")
    private String uploadAllowedRoots;
}
