package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * SSH 凭据配置请求（SVR-13）。私钥/密码只写不读，留空表示不修改。
 */
@Data
public class OperationServerSshVo {

    @ApiModelProperty("SSH 端口，默认 22")
    private Integer sshPort;

    @ApiModelProperty("SSH 登录用户，默认 ubuntu")
    private String sshUser;

    @ApiModelProperty("认证方式：1 私钥 2 密码")
    private Integer sshAuthType;

    @ApiModelProperty("私钥内容（PEM/OpenSSH 文本，留空不改）")
    private String privateKey;

    @ApiModelProperty("私钥口令或登录密码（留空不改）")
    private String passphrase;

    @ApiModelProperty("连接偏好：auto/inner/public")
    private String connPref;
}
