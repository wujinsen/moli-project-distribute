package com.moli.user.center.server.operation.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.dto.operation.OperationDeployBatchTaskRequest;
import com.moli.user.center.common.domain.dto.operation.OperationDeployConstants;
import com.moli.user.center.common.domain.dto.operation.OperationDeployTaskRequest;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.entity.OperationTask;
import com.moli.user.center.common.domain.vo.OperationDeployStatusVo;
import com.moli.user.center.server.operation.config.OperationDeployProperties;
import com.moli.user.center.server.operation.deploy.OperationDeployServiceRegistry;
import com.moli.user.center.server.operation.service.OperationDeployService;
import com.moli.user.center.server.operation.service.OperationRemoteDeployService;
import com.moli.user.center.server.operation.service.OperationServerService;
import com.moli.user.center.server.operation.service.OperationTaskService;
import com.moli.user.center.server.operation.ssh.OperationSshClient;
import com.moli.user.center.server.operation.ssh.OperationSshCommandResult;
import com.moli.user.center.server.operation.ssh.OperationSshSession;
import com.moli.user.center.server.operation.support.OperationBizException;
import com.moli.user.center.server.operation.support.OperationDeployLocalPolicy;
import com.moli.user.center.server.operation.support.OperationDeployTaskProjectSupport;
import com.moli.user.center.server.operation.task.OperationTaskContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 远程启停实现（SVR-15）：SSH 执行 moli-service.sh；serverId 为空时仅 allow-local=true 可本机执行。
 */
@Service
public class OperationRemoteDeployServiceImpl implements OperationRemoteDeployService {

    private static final Set<String> TASK_ACTIONS = OperationDeployConstants.TASK_ACTIONS;
    private static final Set<String> READ_ONLY_ACTIONS = OperationDeployConstants.READ_ONLY_ACTIONS;
    private static final String TASK_TYPE_BATCH = "deploy_batch";
    private static final String BATCH_LOCK_KEY = "deploy_batch:global";

    @Resource
    private OperationDeployProperties deployProperties;
    @Resource
    private OperationDeployServiceRegistry deployServiceRegistry;
    @Resource
    private OperationDeployService operationDeployService;
    @Resource
    private OperationServerService operationServerService;
    @Resource
    private OperationTaskService operationTaskService;
    @Resource
    private OperationSshClient sshClient;
    @Resource
    private OperationDeployLocalPolicy deployLocalPolicy;
    @Resource
    private OperationDeployTaskProjectSupport deployTaskProjectSupport;

    @Override
    public Long createDeployTask(OperationDeployTaskRequest request) {
        OperationDeployTaskProjectSupport.DeployTaskBinding binding =
                deployTaskProjectSupport.resolve(request);
        String key = binding.getServiceKey();
        String act = normalizeAction(request.getAction());
        if (!TASK_ACTIONS.contains(act)) {
            throw OperationBizException.params("异步任务仅支持 start/stop/restart，当前: " + act);
        }
        if (!deployProperties.isEnabled()) {
            throw OperationBizException.deployDisabled();
        }
        Long serverId = binding.getServerId();
        deployLocalPolicy.requireAllowLocalWhenNoServer(serverId);
        OperationServerInfo server = serverId != null ? operationServerService.requireEntity(serverId) : null;

        String target = key + " " + act + (server != null ? " @ " + server.getServerName() : " @ 本机");
        OperationTask task = operationTaskService.create(
                "deploy", serverId, binding.getProjectId(), key, act, target);
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

    @Override
    public Long createBatchDeployTask(OperationDeployBatchTaskRequest batch) {
        if (!deployProperties.isEnabled()) {
            throw OperationBizException.deployDisabled();
        }
        List<OperationDeployTaskRequest> steps = batch.getSteps();
        Long batchProjectId = batch.getProjectId();
        for (OperationDeployTaskRequest step : steps) {
            if (step.getProjectId() == null && batchProjectId != null) {
                step.setProjectId(batchProjectId);
            }
            String act = normalizeAction(step.getAction());
            if (!TASK_ACTIONS.contains(act)) {
                throw OperationBizException.params("批量任务每步仅支持 start/stop/restart，当前: " + act);
            }
            deployTaskProjectSupport.resolve(step);
            deployLocalPolicy.requireAllowLocalWhenNoServer(step.getServerId());
        }

        int total = steps.size();
        String target = "批量滚动重启 " + total + " 步";
        OperationTask task = operationTaskService.create(
                TASK_TYPE_BATCH, null, batchProjectId, null, "batch", target);
        boolean stopOnFailure = !Boolean.FALSE.equals(batch.getStopOnFailure());
        int intervalSeconds = batch.getIntervalSeconds() != null ? batch.getIntervalSeconds() : 0;

        operationTaskService.submit(task.getId(), BATCH_LOCK_KEY, context ->
                executeBatchDeploy(context, steps, stopOnFailure, intervalSeconds));
        return task.getId();
    }

    private void executeBatchDeploy(OperationTaskContext context, List<OperationDeployTaskRequest> steps,
                                    boolean stopOnFailure, int intervalSeconds) throws Exception {
        int total = steps.size();
        context.appendLine("[BATCH] 开始批量滚动重启，共 " + total + " 步");
        int succeeded = 0;
        int failed = 0;
        for (int i = 0; i < total; i++) {
            context.throwIfCancelled();
            OperationDeployTaskRequest stepReq = steps.get(i);
            OperationDeployTaskProjectSupport.DeployTaskBinding binding = deployTaskProjectSupport.resolve(stepReq);
            String key = binding.getServiceKey();
            String act = normalizeAction(stepReq.getAction());
            int stepNo = i + 1;
            context.appendLine("");
            context.appendLine("[BATCH] --- 步骤 " + stepNo + "/" + total + ": " + key + " " + act + " ---");
            context.setProgress(Math.max(1, (i * 100) / total));
            try {
                Long serverId = binding.getServerId();
                OperationServerInfo server = serverId != null ? operationServerService.requireEntity(serverId) : null;
                if (server == null) {
                    runLocal(context, key, act);
                } else {
                    runRemote(context, server, key, act);
                }
                succeeded++;
                context.appendLine("[BATCH] 步骤 " + stepNo + " 成功");
            } catch (Exception e) {
                failed++;
                context.appendLine("[BATCH] 步骤 " + stepNo + " 失败: "
                        + StringUtils.defaultString(e.getMessage(), e.getClass().getSimpleName()));
                if (stopOnFailure) {
                    if (e instanceof BaseException) {
                        throw (BaseException) e;
                    }
                    throw new BaseException(e.getMessage());
                }
            }
            context.setProgress(((i + 1) * 100) / total);
            if (i < total - 1 && intervalSeconds > 0) {
                context.appendLine("[BATCH] 等待 " + intervalSeconds + " 秒...");
                sleepWithCancel(context, intervalSeconds);
            }
        }
        context.appendLine("[BATCH] 完成：成功 " + succeeded + "，失败 " + failed);
        if (failed > 0) {
            throw new BaseException("批量任务部分失败：" + failed + "/" + total);
        }
    }

    private void runLocal(OperationTaskContext context, String key, String act) throws Exception {
        context.throwIfCancelled();
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
        context.throwIfCancelled();
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
        if (context != null) {
            context.appendLine("[SSH] 远端脚本缺失，自动上传: " + remoteScript);
        }
        Path local = resolveLocalScript();
        if (local == null) {
            throw new BaseException("远端脚本缺失且本机无副本可上传: " + remoteScript);
        }
        String content = new String(Files.readAllBytes(local), StandardCharsets.UTF_8);
        sshClient.sftpPutText(session, content, remoteScript, true);
        if (context != null) {
            context.appendLine("[SSH] 脚本上传完成并已 chmod +x");
        }
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
        String key = deployServiceRegistry.requireKnownKey(serviceKey);
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
            ensureRemoteScript(null, session, remoteScript);
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
        } catch (Exception e) {
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

    private String normalizeAction(String action) {
        String act = OperationDeployConstants.normalizeAction(action);
        if (act == null) {
            throw OperationBizException.params("action 不能为空");
        }
        return act;
    }

    private static void sleepWithCancel(OperationTaskContext context, int intervalSeconds)
            throws Exception {
        for (int s = 0; s < intervalSeconds; s++) {
            context.throwIfCancelled();
            Thread.sleep(1000L);
        }
    }
}
