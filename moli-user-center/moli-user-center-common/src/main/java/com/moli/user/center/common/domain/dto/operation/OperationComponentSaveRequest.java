package com.moli.user.center.common.domain.dto.operation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class OperationComponentSaveRequest {

    @ApiModelProperty("主键（更新时必填）")
    private Long id;

    @ApiModelProperty("服务器 ID（推荐；与 serverIp 二选一）")
    private Long serverId;

    @Size(max = 64, message = "serverIp 过长")
    private String serverIp;

    @NotBlank(message = "componentName 不能为空")
    @Size(max = 256, message = "componentName 过长")
    private String componentName;

    @Size(max = 256, message = "account 过长")
    private String account;

    @Size(max = 256, message = "password 过长")
    private String password;

    @Size(max = 512, message = "deployPath 过长")
    private String deployPath;

    @Pattern(regexp = "^$|^\\d{1,5}$|^-$", message = "port 格式不正确")
    private String port;

    @Size(max = 64, message = "version 过长")
    private String version;

    @OperationEnvironment
    private Integer environment;

    @Size(max = 512, message = "remark 过长")
    private String remark;

    @AssertTrue(message = "serverId 与 serverIp 至少填一项")
    public boolean isServerRefPresent() {
        return serverId != null || StringUtils.isNotBlank(serverIp);
    }
}
