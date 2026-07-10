package com.moli.user.center.server.operation.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.entity.OperationTask;
import com.moli.user.center.common.domain.vo.OperationDeployStatusVo;
import com.moli.user.center.server.operation.config.OperationDeployProperties;
import com.moli.user.center.server.operation.service.OperationDeployService;
import com.moli.user.center.server.operation.service.OperationRemoteDeployService;
import com.moli.user.center.server.operation.service.OperationServerService;
import com.moli.user.center.server.operation.service.OperationTaskService;
import com.moli.user.center.server.operation.ssh.OperationSshClient;
import com.moli.user.center.server.operation.ssh.OperationSshCommandResult;
import com.moli.user.center.server.operation.ssh.OperationSshSession;
import com.moli.user.center.server.operation.task.OperationTaskContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 远程启停实现（SVR-15）：SSH 执行 moli-service.sh；脚本缺失自动上传就位；serverId 为空回退本机。
 */
@Service
public class OperationRemoteDeployServiceImpl implements OperationRemoteDeployService {

    private static final Set<String> SERVICE_KEYS = new HashSet<>(Arrays.asList("user-center", "gateway", "knowledge"));
    private static final Set<String> TASK_ACTIONS = new HashSet<>(Arrays.asList("start", "stop", "restart"));
    private static final Set<String> READ_ONLY_ACTIONS = new HashSet<>(Arrays.asList("status", "logs"));

    @Resource
    private OperationDeployProperties deployProperties;
    @Resource
    private OperationDeployService operationDeployService;
    @Resource
    private OperationServerService operationServerService;
    @Resource
    private OperationTaskService operationTaskService;
    @Resource
    private OperationSshClient sshClient;

    @Override
    public Long createDeployTask(Long serverId, String serviceKey, String action) {
        String key = normalizeServiceKey(serviceKey);
        String act = normalizeAction(action);
        if (!TASK_ACTIONS.contains(act)) {
            throw new BaseException("异步任务仅支持 start/stop/restart，当前: " + act);
        }
        if (!deployProperties.isEnabled()) {
            throw new BaseException("部署变更动作未启用，请配置 ops.deploy.enabled=true");
        }
        OperationServerInfo server = serverId != null ? operationServerService.requireEntity(serverId) : null;

        String target = key + " " + act + (server != null ? " @ " + server.getServerName() : " @ 本机");
        OperationTask task = operationTaskService.create("deploy", serverId, key, act, target);
        String lockKey = "deploy:" + (serverId == null ? "local" : serverId) + ":" + key;

        operationTaskService.submit(task.getId(), lockKey, context -> {
            if (server == null) {
                runLocal(context, key, act);
            } else {
                runRemote(context, server, key, act);
            }
        });
        return task.getId();
    }

    private void runLocal(OperationTaskContext context, String key, String act) {
        context.appendLine("[本机] 执行 moli-service.sh " + key + " " + act);
        context.setProgress(20);
        OperationDeployStatusVo vo = operationDeployService.execute(key, act, null);
        if (StringUtils.isNotBlank(vo.getOutput())) {
            for (String line : vo.getOutput().split("\n")) {
                context.appendLine(line);
            }
        }
        context.setProgress(90);
        context.appendLine("[本机] " + StringUtils.defaultString(vo.getMessage()));
        if (!Boolean.TRUE.equals(vo.getAvailable())) {
            throw new BaseException(StringUtils.defaultIfBlank(vo.getMessage(), "本机执行失败"));
        }
    }

    private void runRemote(OperationTaskContext context, OperationServerInfo server,
                           String key, String act) throws Exception {
        context.appendLine("[SSH] 连接 " + server.getServerName() + " ...");
        context.setProgress(10);
        try (OperationSshSession session = sshClient.connect(server)) {
            context.appendLine("[SSH] 已连接 " + session.getHost());
            context.setProgress(25);

            String remoteScript = remoteScriptPath();
            ensureRemoteScript(context, session, remoteScript);
            context.setProgress(40);

            String command = "bash " + OperationSshClient.shellQuote(remoteScript) + " " + key + " " + act;
            context.appendLine("[SSH] " + command);
            OperationSshCommandResult result = sshClient.exec(session, command, context::appendLine);
            context.setProgress(95);
            if (!result.isSuccess()) {
                throw new BaseException("远程脚本返回非零退出码: " + result.getExitCode());
            }
            context.appendLine("[SSH] 执行完成");
        }
    }

    /**
     * 远端脚本缺失时，从本机部署目录上传并 chmod +x（自动转换 CRLF）。
     */
    private void ensureRemoteScript(OperationTaskContext context, OperationSshSession session,
                                    String remoteScript) throws Exception {
        if (sshClient.sftpExists(session, remoteScript)) {
            return;
        }
        context.appendLine("[SSH] 远端脚本缺失，自动上传: " + remoteScript);
        Path local = resolveLocalScript();
        if (local == null) {
            throw new BaseException("远端脚本缺失且本机无副本可上传: " + remoteScript);
        }
        String content = new String(Files.readAllBytes(local), StandardCharsets.UTF_8);
        sshClient.sftpPutText(session, content, remoteScript, true);
        context.appendLine("[SSH] 脚本上传完成并已 chmod +x");
    }

    private Path resolveLocalScript() {
        if (StringUtils.isNotBlank(deployProperties.getScriptPath())) {
            Path p = Paths.get(deployProperties.getScriptPath());
            if (Files.exists(p)) {
                return p;
            }
        }
        Path byRoot = Paths.get(deployProperties.getDeployRoot(), "deploy", "linux", "moli-service.sh");
        if (Files.exists(byRoot)) {
            return byRoot;
        }
        // 开发机运行时回退到工作目录相对路径
        Path byCwd = Paths.get("deploy", "linux", "moli-service.sh");
        return Files.exists(byCwd) ? byCwd : null;
    }

    private String remoteScriptPath() {
        return deployProperties.getDeployRoot() + "/deploy/linux/moli-service.sh";
    }

    @Override
    public OperationDeployStatusVo executeRemoteReadOnly(Long serverId, String serviceKey, String action, String extraArg) {
        String key = normalizeServiceKey(serviceKey);
        String act = normalizeAction(action);
        if (!READ_ONLY_ACTIONS.contains(act)) {
            throw new BaseException("远程同步调用仅支持 status/logs，当前: " + act);
        }
        OperationServerInfo server = operationServerService.requireEntity(serverId);

        OperationDeployStatusVo vo = new OperationDeployStatusVo();
        vo.setServiceKey(key);
        vo.setAction(act);
        try (OperationSshSession session = sshClient.connect(server)) {
            String remoteScript = remoteScriptPath();
            if (!sshClient.sftpExists(session, remoteScript)) {
                vo.setAvailable(false);
                vo.setRunning(false);
                vo.setMessage("远端部署脚本不存在: " + remoteScript);
                return vo;
            }
            StringBuilder command = new StringBuilder("bash ")
                    .append(OperationSshClient.shellQuote(remoteScript))
                    .append(' ').append(key).append(' ').append(act);
            if ("logs".equals(act)) {
                int lines = 200;
                if (StringUtils.isNumeric(StringUtils.trimToEmpty(extraArg))) {
                    lines = Math.min(2000, Integer.parseInt(extraArg.trim()));
                }
                command.append(' ').append(lines);
            }
            OperationSshCommandResult result = sshClient.exec(session, command.toString(), null);
            vo.setAvailable(true);
            vo.setOutput(result.getOutput());
            vo.setRunning(parseRunning(result.getOutput(), result.getExitCode()));
            vo.setMessage(result.isSuccess() ? "执行成功" : "脚本返回非零退出码");
            return vo;
        } catch (BaseException e) {
            vo.setAvailable(false);
            vo.setRunning(false);
            vo.setMessage(e.getMessage());
            return vo;
        }
    }

    private boolean parseRunning(String output, int exitCode) {
        if (StringUtils.isBlank(output)) {
            return exitCode == 0;
        }
        String lower = output.toLowerCase(Locale.ROOT);
        if (lower.contains("[stopped]") || lower.contains("is not running")) {
            return false;
        }
        return lower.contains("is running") || lower.contains("[ok]");
    }

    private String normalizeServiceKey(String serviceKey) {
        if (StringUtils.isBlank(serviceKey)) {
            throw new BaseException("serviceKey 不能为空");
        }
        String key = serviceKey.trim().toLowerCase(Locale.ROOT);
        if (!SERVICE_KEYS.contains(key)) {
            throw new BaseException("不支持的 serviceKey: " + serviceKey);
        }
        return key;
    }

    private String normalizeAction(String action) {
        if (StringUtils.isBlank(action)) {
            throw new BaseException("action 不能为空");
        }
        return action.trim().toLowerCase(Locale.ROOT);
    }
}
