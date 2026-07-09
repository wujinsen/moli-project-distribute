package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class OperationComponentTopologyItemVo {

    private Long id;
    private String componentName;
    private String serverIp;
    private String port;
    private String version;
    private String deployPath;
    private Integer environment;

    @ApiModelProperty("健康状态")
    private Integer status;
    private Date lastCheckTime;
}
