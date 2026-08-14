package com.moli.user.center.common.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class OperationRelationTaskItemVo {

    private Long id;
    private String taskType;
    private String action;
    private String status;
    private Date createTime;
}
