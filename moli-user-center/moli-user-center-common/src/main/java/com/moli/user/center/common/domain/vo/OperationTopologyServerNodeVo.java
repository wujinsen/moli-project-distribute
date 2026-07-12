package com.moli.user.center.common.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class OperationTopologyServerNodeVo {

    private String id;
    private Long serverId;
    private String serverName;
    private String ip;
    private String innerIp;
    private Integer environment;
    private String serverRole;
    private List<String> tags;
    private Integer status;
}
