package com.moli.user.center.server.operation.service;

/**
 * 远程 shell 命令执行（SVR-18）。
 */
public interface OperationCommandService {

    /**
     * 创建异步远程命令任务。
     *
     * @return taskId
     */
    Long createCommandTask(Long serverId, String command, String workDir);
}
