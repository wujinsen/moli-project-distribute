package com.moli.user.center.server.operation.service;

import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.entity.OperationTask;
import com.moli.user.center.common.domain.vo.OperationTaskVo;
import com.moli.user.center.server.operation.task.OperationTaskJob;

public interface OperationTaskService {

    /** 创建 pending 任务并落库，返回带 ID 的实体。 */
    OperationTask create(String taskType, Long serverId, String serviceKey, String action, String targetName);

    /**
     * 提交任务到线程池异步执行；同一 serverId+serviceKey 有运行中任务时拒绝。
     * @param lockKey 并发互斥键（可空表示不互斥）
     */
    void submit(Long taskId, String lockKey, OperationTaskJob job);

    /** 轮询任务：返回状态/进度 + 从 logOffset 起的增量日志。 */
    OperationTaskVo poll(Long taskId, int logOffset);

    /** 任务历史分页。 */
    PageRes<OperationTaskVo> list(String taskType, Long serverId, Integer pageNum, Integer pageSize);
}
