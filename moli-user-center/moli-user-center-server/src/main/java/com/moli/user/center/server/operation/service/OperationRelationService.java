package com.moli.user.center.server.operation.service;

import com.moli.user.center.common.domain.vo.OperationRelationsVo;

public interface OperationRelationService {

    OperationRelationsVo getRelations(String entityType, Long id);
}
