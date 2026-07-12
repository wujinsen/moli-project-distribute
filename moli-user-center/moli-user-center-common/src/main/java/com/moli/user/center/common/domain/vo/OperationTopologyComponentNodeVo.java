package com.moli.user.center.common.domain.vo;

import lombok.Data;

@Data
public class OperationTopologyComponentNodeVo {

    private String id;
    private Long componentId;
    private String componentName;
    private String port;
    private String version;
    private Integer environment;
    private Integer status;
    private Integer portMatchStatus;
}
