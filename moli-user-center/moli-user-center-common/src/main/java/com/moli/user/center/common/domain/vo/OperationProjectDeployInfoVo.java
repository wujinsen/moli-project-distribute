package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OperationProjectDeployInfoVo {

    private Long id;
    private Long serverId;
    private String serverIp;
    private String innerIp;
    private String url;
    private String projectName;
    private String deployPath;
    private String port;
    private Integer environment;
    private String remark;
}
