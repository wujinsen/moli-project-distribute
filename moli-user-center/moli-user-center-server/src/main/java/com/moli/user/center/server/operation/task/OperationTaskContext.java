package com.moli.user.center.server.operation.task;

/**
 * 任务执行上下文（SVR-14）：供任务体追加日志与更新进度。
 * <p>实现由 {@link OperationTaskService} 提供；日志写入内存缓冲并节流落库。</p>
 */
public interface OperationTaskContext {

    /** 追加一行日志（自动补换行）。 */
    void appendLine(String line);

    /** 设置进度 0-100。 */
    void setProgress(int progress);

    /** 当前进度。 */
    int getProgress();
}
