package com.moli.user.center.common.domain.dto.operation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class OperationPlatformSaveRequest {

    @ApiModelProperty("主键（更新时必填）")
    private Long id;

    @NotBlank(message = "platformName 不能为空")
    @Size(max = 256, message = "platformName 过长")
    private String platformName;

    @Size(max = 512, message = "url 过长")
    private String url;

    @Size(max = 256, message = "account 过长")
    private String account;

    @Size(max = 256, message = "password 过长")
    private String password;

    @OperationEnvironment
    private Integer environment;

    @Size(max = 512, message = "remark 过长")
    private String remark;
}
