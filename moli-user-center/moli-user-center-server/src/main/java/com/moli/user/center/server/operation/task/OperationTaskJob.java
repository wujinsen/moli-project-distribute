package com.moli.user.center.server.operation.task;

/**
 * 异步任务体（SVR-14）。抛出异常即判定任务失败。
 */
@FunctionalInterface
public interface OperationTaskJob {

    void run(OperationTaskContext context) throws Exception;
}
