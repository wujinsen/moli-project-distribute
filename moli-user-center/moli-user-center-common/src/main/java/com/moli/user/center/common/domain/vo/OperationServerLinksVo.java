package com.moli.user.center.common.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OperationServerLinksVo {

    private Long serverId;
    private List<Long> projectIds = new ArrayList<>();
    private List<Long> componentIds = new ArrayList<>();
}
