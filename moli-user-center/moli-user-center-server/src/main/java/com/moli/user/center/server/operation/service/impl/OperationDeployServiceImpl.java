package com.moli.user.center.server.operation.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.dto.operation.OperationDeployConstants;
import com.moli.user.center.common.domain.vo.OperationDeployStatusVo;
import com.moli.user.center.server.operation.config.OperationDeployProperties;
import com.moli.user.center.server.operation.deploy.OperationDeployServiceRegistry;
import com.moli.user.center.server.operation.service.OperationDeployService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class OperationDeployServiceImpl implements OperationDeployService {

    private static final Set<String> READ_ONLY_ACTIONS = OperationDeployConstants.READ_ONLY_ACTIONS;
    private static final Set<String> MUTATING_ACTIONS = OperationDeployConstants.TASK_ACTIONS;

    @Resource
    private OperationDeployProperties deployProperties;
    @Resource
    private OperationDeployServiceRegistry deployServiceRegistry;

    @Override
    public OperationDeployStatusVo status(String serviceKey) {
        return execute(serviceKey, "status", null);
    }

    @Override
    public OperationDeployStatusVo execute(String serviceKey, String action, String extraArg) {
        String key = deployServiceRegistry.normalizeServiceKey(serviceKey);
        String act = normalizeAction(action);
        validateActionAllowed(act);

        OperationDeployStatusVo vo = new OperationDeployStatusVo();
        vo.setServiceKey(key);
        vo.setAction(act);

        Path script = resolveScriptPath();
        if (!Files.exists(script)) {
            vo.setAvailable(false);
            vo.setRunning(false);
            vo.setMessage("部署脚本不存在: " + script);
            return vo;
        }
        if (isWindows()) {
            vo.setAvailable(false);
            vo.setRunning(false);
            vo.setMessage("当前环境为 Windows，请在 Linux 部署节点调用 moli-service.sh");
            return vo;
        }

        try {
            ProcessBuilder builder = new ProcessBuilder(buildCommand(script, key, act, extraArg));
            builder.redirectErrorStream(true);
            Process process = builder.start();

            boolean finished = process.waitFor(deployProperties.getTimeoutSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BaseException("部署脚本执行超时");
            }

            String output = readOutput(process);
            vo.setAvailable(true);
            vo.setOutput(output);
            vo.setRunning(parseRunning(output, process.exitValue()));
            vo.setMessage(process.exitValue() == 0 ? "执行成功" : "脚本返回非零退出码");
            return vo;
        } catch (BaseException ex) {
            throw ex;
        } catch (Exception ex) {
            vo.setAvailable(false);
            vo.setRunning(false);
            vo.setMessage("执行失败: " + ex.getMessage());
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

    private String readOutput(Process process) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private String[] buildCommand(Path script, String serviceKey, String action, String extraArg) {
        if ("logs".equals(action) && StringUtils.isNotBlank(extraArg)) {
            return new String[]{"bash", script.toString(), serviceKey, action, extraArg};
        }
        return new String[]{"bash", script.toString(), serviceKey, action};
    }

    private void validateActionAllowed(String action) {
        if (READ_ONLY_ACTIONS.contains(action)) {
            return;
        }
        if (MUTATING_ACTIONS.contains(action)) {
            if (!deployProperties.isEnabled()) {
                throw new BaseException("部署变更动作未启用，请配置 ops.deploy.enabled=true");
            }
            return;
        }
        throw new BaseException("不支持的部署动作: " + action);
    }

    private Path resolveScriptPath() {
        if (StringUtils.isNotBlank(deployProperties.getScriptPath())) {
            return Paths.get(deployProperties.getScriptPath());
        }
        return Paths.get(deployProperties.getDeployRoot(), "deploy", "linux", "moli-service.sh");
    }

    private String normalizeAction(String action) {
        if (StringUtils.isBlank(action)) {
            throw new BaseException("action 不能为空");
        }
        return action.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }
}
