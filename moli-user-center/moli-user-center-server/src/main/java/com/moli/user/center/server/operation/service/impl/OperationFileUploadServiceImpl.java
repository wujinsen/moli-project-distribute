package com.moli.user.center.server.operation.service.impl;

import com.jcraft.jsch.SftpProgressMonitor;
import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.entity.OperationTask;
import com.moli.user.center.server.operation.config.OperationDeployProperties;
import com.moli.user.center.server.operation.config.OperationUploadProperties;
import com.moli.user.center.server.operation.service.OperationFileUploadService;
import com.moli.user.center.server.operation.service.OperationServerService;
import com.moli.user.center.server.operation.service.OperationTaskService;
import com.moli.user.center.server.operation.ssh.OperationSshClient;
import com.moli.user.center.server.operation.ssh.OperationSshCommandResult;
import com.moli.user.center.server.operation.ssh.OperationSshSession;
import com.moli.user.center.server.operation.task.OperationTaskContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 文件上传发布实现（SVR-16）：
 * 本地暂存 → SFTP 上传（进度回写）→ 白名单后置动作（nginxReload / unzipToDist / restartService:{key}）。
 */
@Service
public class OperationFileUploadServiceImpl implements OperationFileUploadService {

    private static final Set<String> SERVICE_KEYS = new HashSet<>(Arrays.asList("user-center", "gateway", "knowledge"));
    /** SFTP 上传占任务进度的区间上限，剩余留给后置动作。 */
    private static final int UPLOAD_PROGRESS_CAP = 80;

    @Resource
    private OperationUploadProperties uploadProperties;
    @Resource
    private OperationDeployProperties deployProperties;
    @Resource
    private OperationServerService operationServerService;
    @Resource
    private OperationTaskService operationTaskService;
    @Resource
    private OperationSshClient sshClient;

    @Override
    public Long createUploadTask(MultipartFile file, Long serverId, String targetPath, String postAction) {
        if (!uploadProperties.isEnabled()) {
            throw new BaseException("文件上传发布未启用，请配置 ops.upload.enabled=true");
        }
        if (file == null || file.isEmpty()) {
            throw new BaseException("上传文件不能为空");
        }
        if (file.getSize() > uploadProperties.getMaxBytes()) {
            throw new BaseException("文件超过大小上限 " + (uploadProperties.getMaxBytes() / 1024 / 1024) + "MB");
        }
        if (serverId == null) {
            throw new BaseException("serverId 不能为空");
        }
        OperationServerInfo server = operationServerService.requireEntity(serverId);
        String remotePath = resolveRemotePath(targetPath, file.getOriginalFilename());
        String action = normalizePostAction(postAction, remotePath);

        // 请求线程内暂存到本地，异步线程读不到 Multipart 流
        Path temp = saveToTemp(file);
        long size = file.getSize();

        OperationTask task = operationTaskService.create("upload", serverId, null, action,
                remotePath + " (" + humanSize(size) + ")");
        String lockKey = "upload:" + serverId + ":" + remotePath;
        try {
            operationTaskService.submit(task.getId(), lockKey, context ->
                    runUpload(context, server, temp, size, remotePath, action));
        } catch (Exception e) {
            deleteQuietly(temp);
            throw e;
        }
        return task.getId();
    }

    private void runUpload(OperationTaskContext context, OperationServerInfo server,
                           Path temp, long size, String remotePath, String action) throws Exception {
        try {
            context.appendLine("[SSH] 连接 " + server.getServerName() + " ...");
            try (OperationSshSession session = sshClient.connect(server)) {
                context.appendLine("[SSH] 已连接 " + session.getHost());
                context.appendLine("[SFTP] 上传 → " + remotePath + " (" + humanSize(size) + ")");
                try (InputStream in = Files.newInputStream(temp)) {
                    sshClient.sftpPut(session, in, remotePath, new TaskProgressMonitor(context, size));
                }
                context.setProgress(UPLOAD_PROGRESS_CAP);
                context.appendLine("[SFTP] 上传完成");
                executePostAction(context, session, remotePath, action);
            }
        } finally {
            deleteQuietly(temp);
        }
    }

    private void executePostAction(OperationTaskContext context, OperationSshSession session,
                                   String remotePath, String action) {
        if ("none".equals(action)) {
            context.setProgress(100);
            return;
        }
        context.appendLine("[POST] 执行后置动作: " + action);
        String command;
        if ("nginxReload".equals(action)) {
            command = "sudo nginx -t && sudo nginx -s reload";
        } else if ("unzipToDist".equals(action)) {
            command = buildUnzipToDistCommand(remotePath);
        } else if (action.startsWith("restartService:")) {
            String serviceKey = action.substring("restartService:".length());
            String script = deployProperties.getDeployRoot() + "/deploy/linux/moli-service.sh";
            command = "bash " + OperationSshClient.shellQuote(script) + " " + serviceKey + " restart";
        } else {
            throw new BaseException("不支持的后置动作: " + action);
        }
        OperationSshCommandResult result = sshClient.exec(session, command, context::appendLine);
        if (!result.isSuccess()) {
            throw new BaseException("后置动作失败（退出码 " + result.getExitCode() + "）: " + action);
        }
        context.setProgress(100);
        context.appendLine("[POST] 后置动作完成");
    }

    /**
     * 解压 zip 到同目录 dist/：先解到临时目录，备份旧 dist 后原子切换。
     */
    private String buildUnzipToDistCommand(String remoteZipPath) {
        if (!remoteZipPath.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            throw new BaseException("unzipToDist 仅支持 .zip 文件");
        }
        int idx = remoteZipPath.lastIndexOf('/');
        String dir = remoteZipPath.substring(0, idx);
        String qDir = OperationSshClient.shellQuote(dir);
        String qZip = OperationSshClient.shellQuote(remoteZipPath);
        return "set -e; cd " + qDir + "; "
                + "rm -rf .dist-new; mkdir .dist-new; "
                + "unzip -q -o " + qZip + " -d .dist-new; "
                + "if [ -d .dist-new/dist ]; then SRC=.dist-new/dist; else SRC=.dist-new; fi; "
                + "if [ -d dist ]; then mv dist dist.bak.$(date +%Y%m%d%H%M%S); fi; "
                + "mv \"$SRC\" dist; rm -rf .dist-new; "
                + "echo 'dist 已切换，旧版本已备份为 dist.bak.*'";
    }

    /**
     * 目标路径规范化 + 白名单前缀校验。
     */
    private String resolveRemotePath(String targetPath, String originalFilename) {
        if (StringUtils.isBlank(targetPath)) {
            throw new BaseException("targetPath 不能为空");
        }
        String path = targetPath.trim().replace('\\', '/');
        if (!path.startsWith("/")) {
            throw new BaseException("targetPath 必须为绝对路径");
        }
        if (path.contains("..")) {
            throw new BaseException("targetPath 不允许包含 ..");
        }
        if (path.endsWith("/")) {
            String name = StringUtils.isNotBlank(originalFilename)
                    ? Paths.get(originalFilename.replace('\\', '/')).getFileName().toString()
                    : "upload.bin";
            path = path + name;
        }
        final String candidate = path;
        boolean allowed = uploadProperties.getAllowedPaths().stream()
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .anyMatch(candidate::startsWith);
        if (!allowed) {
            throw new BaseException("targetPath 不在白名单内，允许前缀: " + uploadProperties.getAllowedPaths());
        }
        return path;
    }

    private String normalizePostAction(String postAction, String remotePath) {
        String action = StringUtils.defaultIfBlank(postAction, "none").trim();
        if ("none".equals(action) || "nginxReload".equals(action)) {
            return action;
        }
        if ("unzipToDist".equals(action)) {
            if (!remotePath.toLowerCase(Locale.ROOT).endsWith(".zip")) {
                throw new BaseException("unzipToDist 仅支持 .zip 文件");
            }
            return action;
        }
        if (action.startsWith("restartService:")) {
            String serviceKey = action.substring("restartService:".length()).trim().toLowerCase(Locale.ROOT);
            if (!SERVICE_KEYS.contains(serviceKey)) {
                throw new BaseException("不支持的 serviceKey: " + serviceKey);
            }
            if (!deployProperties.isEnabled()) {
                throw new BaseException("重启服务需 ops.deploy.enabled=true");
            }
            return "restartService:" + serviceKey;
        }
        throw new BaseException("不支持的后置动作: " + postAction);
    }

    private Path saveToTemp(MultipartFile file) {
        try {
            Path dir;
            if (StringUtils.isNotBlank(uploadProperties.getTempDir())) {
                dir = Paths.get(uploadProperties.getTempDir());
                Files.createDirectories(dir);
            } else {
                dir = Paths.get(System.getProperty("java.io.tmpdir"));
            }
            Path temp = Files.createTempFile(dir, "moli-ops-upload-", ".tmp");
            file.transferTo(temp.toFile());
            return temp;
        } catch (Exception e) {
            throw new BaseException("暂存上传文件失败: " + e.getMessage());
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
            // 临时文件清理失败不影响任务结果
        }
    }

    private String humanSize(long bytes) {
        if (bytes >= 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1fMB", bytes / 1024.0 / 1024.0);
        }
        if (bytes >= 1024) {
            return String.format(Locale.ROOT, "%.1fKB", bytes / 1024.0);
        }
        return bytes + "B";
    }

    /**
     * SFTP 字节进度 → 任务进度（0 ~ UPLOAD_PROGRESS_CAP）。
     */
    private static class TaskProgressMonitor implements SftpProgressMonitor {

        private final OperationTaskContext context;
        private final long total;
        private long transferred;
        private int lastPercent = -1;

        TaskProgressMonitor(OperationTaskContext context, long total) {
            this.context = context;
            this.total = Math.max(1, total);
        }

        @Override
        public void init(int op, String src, String dest, long max) {
            context.setProgress(1);
        }

        @Override
        public boolean count(long count) {
            transferred += count;
            int percent = (int) (transferred * UPLOAD_PROGRESS_CAP / total);
            if (percent != lastPercent) {
                lastPercent = percent;
                context.setProgress(Math.min(UPLOAD_PROGRESS_CAP, percent));
            }
            return true;
        }

        @Override
        public void end() {
            context.setProgress(UPLOAD_PROGRESS_CAP);
        }
    }
}
