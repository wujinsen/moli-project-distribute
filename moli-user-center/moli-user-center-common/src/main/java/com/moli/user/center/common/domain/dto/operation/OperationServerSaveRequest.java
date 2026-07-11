package com.moli.user.center.common.domain.dto.operation;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class OperationServerSaveRequest {

    @ApiModelProperty("主键（更新时必填）")
    private Long id;

    @NotBlank(message = "serverName 不能为空")
    @Size(max = 256, message = "serverName 过长")
    private String serverName;

    @NotBlank(message = "ip 不能为空")
    @Size(max = 64, message = "ip 过长")
    private String ip;

    @Size(max = 64, message = "innerIp 过长")
    private String innerIp;

    @Size(max = 32, message = "port 过长")
    private String port;

    @OperationEnvironment
    private Integer environment;

    @Size(max = 512, message = "remark 过长")
    private String remark;
}
