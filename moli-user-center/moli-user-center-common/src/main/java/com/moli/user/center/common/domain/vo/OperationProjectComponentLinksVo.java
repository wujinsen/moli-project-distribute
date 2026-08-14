package com.moli.user.center.common.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OperationProjectComponentLinksVo {

    private Long projectId;
    private List<Long> componentIds = new ArrayList<>();
}
