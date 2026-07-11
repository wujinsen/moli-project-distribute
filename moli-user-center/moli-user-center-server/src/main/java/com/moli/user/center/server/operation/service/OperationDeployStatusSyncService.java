package com.moli.user.center.server.operation.service;

import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;

/**
 * 按项目同步 deploy_running（本机脚本或 SSH 远程 status）。
 */
public interface OperationDeployStatusSyncService {

    /**
     * 查询目标机进程状态并写回 project.deployRunning / lastDeployCheckTime。
     *
     * @return true 表示已成功写入 deployRunning（含 false）
     */
    boolean syncProject(OperationProjectDeployInfo project);
}
