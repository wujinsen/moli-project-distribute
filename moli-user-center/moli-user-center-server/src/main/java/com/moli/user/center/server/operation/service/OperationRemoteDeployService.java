package com.moli.user.center.server.operation.service;

import com.moli.user.center.common.domain.vo.OperationDeployStatusVo;

/**
 * 远程启停（SVR-15）：SSH 执行 moli-service.sh，本机回退。
 */
public interface OperationRemoteDeployService {

    /**
     * 创建异步启停任务（start/stop/restart）。serverId 为空时在本机执行。
     * @return taskId
     */
    Long createDeployTask(Long serverId, String serviceKey, String action);

    /**
     * 远程同步只读动作（status / logs），SSH 执行后直接返回输出。
     */
    OperationDeployStatusVo executeRemoteReadOnly(Long serverId, String serviceKey, String action, String extraArg);
}
