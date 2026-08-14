package com.moli.user.center.common.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class OperationRelationServerItemVo {

    private Long id;
    private String serverName;
    private String ip;
    private String innerIp;
    private Integer environment;
    private String serverRole;
    private List<String> tags;
    private Integer status;

    @ApiModelProperty("是否为主 server_id")
    private Boolean primary;
}
