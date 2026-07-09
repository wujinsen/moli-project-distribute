package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

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
    private String remark;

    @ApiModelProperty("健康状态 0未知 1可达 2不可达 3跳过")
    private Integer status;
    private Date lastCheckTime;
}
