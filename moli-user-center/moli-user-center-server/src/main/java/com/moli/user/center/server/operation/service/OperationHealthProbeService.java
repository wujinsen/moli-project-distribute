package com.moli.user.center.server.operation.service;

import com.moli.user.center.common.domain.vo.OperationHealthProbeResultVo;

public interface OperationHealthProbeService {

    /** 定时任务等内部同步探活（不走 operation_task）。 */
    OperationHealthProbeResultVo probeAll();

    /** HTTP 触发：创建 health_probe 异步任务，立即返回 taskId。 */
    Long createProbeAllTask();
}
