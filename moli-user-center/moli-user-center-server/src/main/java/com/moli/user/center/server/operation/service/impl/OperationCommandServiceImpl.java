package com.moli.user.center.server.operation.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.entity.OperationTask;
import com.moli.user.center.server.operation.config.OperationCommandProperties;
import com.moli.user.center.server.operation.guard.OperationShellGuard;
import com.moli.user.center.server.operation.service.OperationCommandService;
import com.moli.user.center.server.operation.service.OperationServerService;
import com.moli.user.center.server.operation.service.OperationTaskService;
import com.moli.user.center.server.operation.ssh.OperationSshClient;
import com.moli.user.center.server.operation.ssh.OperationSshCommandResult;
import com.moli.user.center.server.operation.ssh.OperationSshSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OperationCommandServiceImpl implements OperationCommandService {

    @Resource
    private OperationCommandProperties commandProperties;
    @Resource
    private OperationServerService operationServerService;
    @Resource
    private OperationTaskService operationTaskService;
    @Resource
    private OperationSshClient sshClient;

    @Override
    public Long createCommandTask(Long serverId, String command, String workDir) {
        if (!commandProperties.isEnabled()) {
            throw new BaseException("远程命令执行未启用，请配置 ops.command.enabled=true");
        }
        if (serverId == null) {
            throw new BaseException("serverId 不能为空");
        }
        OperationServerInfo server = operationServerService.requireEntity(serverId);
        String work = StringUtils.isNotBlank(workDir) ? workDir : commandProperties.getDefaultWorkDir();
        String remoteCommand = OperationShellGuard.buildRemoteCommand(
                OperationShellGuard.validateCommand(command, commandProperties.getMaxChars()),
                work);
        String audit = OperationShellGuard.abbreviateForAudit(command, 200);

        OperationTask task = operationTaskService.create("command", serverId, null, "exec",
                server.getServerName() + " · " + audit);
        String lockKey = "command:" + serverId;
        operationTaskService.submit(task.getId(), lockKey, context -> runCommand(context, server, remoteCommand));
        return task.getId();
    }

    private void runCommand(com.moli.user.center.server.operation.task.OperationTaskContext context,
                            OperationServerInfo server, String remoteCommand) throws Exception {
        context.appendLine("[SSH] 连接 " + server.getServerName() + " ...");
        try (OperationSshSession session = sshClient.connect(server)) {
            context.appendLine("[SSH] 已连接 " + session.getHost());
            context.appendLine("[CMD] " + remoteCommand);
            context.setProgress(10);
            OperationSshCommandResult result = sshClient.exec(session, remoteCommand, context::appendLine);
            if (!result.isSuccess()) {
                throw new BaseException("命令执行失败（退出码 " + result.getExitCode() + "）");
            }
            context.setProgress(100);
            context.appendLine("[CMD] 执行完成");
        }
    }
}
