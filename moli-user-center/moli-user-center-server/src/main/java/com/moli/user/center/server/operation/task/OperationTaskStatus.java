package com.moli.user.center.server.operation.task;

/**
 * 任务状态常量。
 */
public final class OperationTaskStatus {

    public static final String PENDING = "pending";
    public static final String RUNNING = "running";
    public static final String SUCCESS = "success";
    public static final String FAILED = "failed";
    public static final String CANCELLED = "cancelled";

    private OperationTaskStatus() {
    }
}
