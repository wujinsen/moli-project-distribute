package com.moli.user.center.common.domain.vo;

import lombok.Data;

@Data
public class OperationTopologyProjectNodeVo {

    private String id;
    private Long projectId;
    private String projectName;
    private String port;
    private Integer environment;
    private Boolean deployRunning;
    private Integer portMatchStatus;
}
