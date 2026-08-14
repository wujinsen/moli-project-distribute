package com.moli.user.center.server.operation.service;

import com.moli.user.center.common.domain.vo.OperationRelationReconcileVo;

public interface OperationRelationRepairService {

    /**
     * 将主表 server_id 与 N:N 首台对齐（仅处理 N:N 非空且主表不一致的台账）。
     */
    OperationRelationReconcileVo reconcilePrimaryServers();
}
