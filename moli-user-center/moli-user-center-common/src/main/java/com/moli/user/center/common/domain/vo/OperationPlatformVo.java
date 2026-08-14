package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class OperationPlatformVo {

    private Long id;
    private Long createId;
    private Date createTime;
    private Long updateId;
    private Date updateTime;

    @ApiModelProperty("平台名称")
    private String platformName;
    @ApiModelProperty("url")
    private String url;
    @ApiModelProperty("账户名")
    private String account;
    @ApiModelProperty("开发环境: 1: dev 2:test 3:pre 4:pro")
    private Integer environment;
    private String remark;

    @ApiModelProperty("是否已配置密码")
    private Boolean passwordConfigured;
    @ApiModelProperty("密码掩码（末四位）")
    private String passwordMask;
}
