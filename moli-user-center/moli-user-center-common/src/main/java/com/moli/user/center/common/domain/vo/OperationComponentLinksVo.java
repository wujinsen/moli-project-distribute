package com.moli.user.center.common.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OperationComponentLinksVo {

    private Long componentId;
    private List<Long> serverIds = new ArrayList<>();
}
