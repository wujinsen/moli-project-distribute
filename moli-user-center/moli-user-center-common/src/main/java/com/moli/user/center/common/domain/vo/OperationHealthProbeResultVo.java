package com.moli.user.center.common.domain.vo;

import lombok.Data;

@Data
public class OperationHealthProbeResultVo {

    private int serversProbed;
    private int componentsProbed;
    private int deployStatusesSynced;
    private int serverIdsSynced;
}
