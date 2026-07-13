package com.moli.user.center.server.operation.task;

/**
 * 任务被用户取消（协作式退出）。
 */
public class OperationTaskCancelledException extends Exception {

    public OperationTaskCancelledException() {
        super("任务已取消");
    }
}
