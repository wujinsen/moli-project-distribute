package com.moli.user.center.common.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OperationRelationsVo {

    private String entityType;
    private OperationRelationEntityVo entity;
    private List<OperationRelationServerItemVo> servers = new ArrayList<>();
    private List<OperationRelationProjectItemVo> projects = new ArrayList<>();
    private List<OperationRelationComponentItemVo> components = new ArrayList<>();
    private List<OperationRelationTaskItemVo> recentTasks = new ArrayList<>();
}
