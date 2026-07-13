package com.moli.user.center.server.operation.service.impl;

import com.jcraft.jsch.SftpProgressMonitor;
import com.moli.common.constant.PermissionConstants;
import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.dto.operation.OperationFileUploadRequest;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.entity.OperationTask;
import com.moli.user.center.server.operation.config.OperationCommandProperties;
import com.moli.user.center.server.operation.config.OperationDeployProperties;
import com.moli.user.center.server.operation.config.OperationUploadProperties;
import com.moli.user.center.server.operation.deploy.OperationDeployServiceRegistry;
import com.moli.user.center.server.operation.guard.OperationPathPolicy;
import com.moli.user.center.server.operation.guard.OperationShellGuard;
import com.moli.user.center.server.operation.service.OperationFileUploadService;
import com.moli.user.center.server.operation.service.OperationServerService;
import com.moli.user.center.server.operation.service.OperationTaskService;
import com.moli.user.center.server.operation.ssh.OperationSshClient;
import com.moli.user.center.server.operation.ssh.OperationSshCommandResult;
import com.moli.user.center.server.operation.ssh.OperationSshSession;
import com.moli.user.center.server.operation.support.OperationBizException;
import com.moli.user.center.server.operation.task.OperationTaskContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * 文件上传发布（SVR-16/19）：SFTP + 快捷预设或自定义 shell 后置。
 */
@Service
public class OperationFileUploadServiceImpl implements OperationFileUploadService {

    private static final int UPLOAD_PROGRESS_CAP = 80;

    @Resource
    private OperationUploadProperties uploadProperties;
    @Resource
    private OperationDeployProperties deployProperties;
    @Resource
    private OperationCommandProperties commandProperties;
    @Resource
    private OperationServerService operationServerService;
    @Resource
    private OperationTaskService operationTaskService;
    @Resource
    private OperationSshClient sshClient;
    @Resource
    private OperationDeployServiceRegistry deployServiceRegistry;

    @Override
    public Long createUploadTask(MultipartFile file, OperationFileUploadRequest request) {
        if (!uploadProperties.isEnabled()) {
            throw OperationBizException.uploadDisabled();
        }
        if (file == null || file.isEmpty()) {
            throw OperationBizException.params("上传文件不能为空");
        }
        if (file.getSize() > uploadProperties.getMaxBytes()) {
            throw OperationBizException.params("文件超过大小上限 " + (uploadProperties.getMaxBytes() / 1024 / 1024) + "MB");
        }
        Long serverId = request.getServerId();
        String targetPath = request.getTargetPath();
        String postAction = StringUtils.defaultIfBlank(request.getPostAction(), "none");
        String postCommand = request.getPostCommand();
        OperationServerInfo server = operationServerService.requireEntity(serverId);
        String remotePath = OperationPathPolicy.resolveRemotePath(targetPath, file.getOriginalFilename(),
                server, uploadProperties);
        PostActionSpec spec = normalizePostAction(postAction, postCommand, remotePath);

        Path temp = saveToTemp(file);
        long size = file.getSize();

        OperationTask task = operationTaskService.create("upload", serverId, null, null, spec.actionLabel,
                remotePath + " (" + humanSize(size) + ")");
        String lockKey = "upload:" + serverId + ":" + remotePath;
        try {
            operationTaskService.submit(task.getId(), lockKey, context ->
                    runUpload(context, server, temp, size, remotePath, spec));
        } catch (Exception e) {
            deleteQuietly(temp);
            throw e;
        }
        return task.getId();
    }

    private void runUpload(OperationTaskContext context, OperationServerInfo server,
                           Path temp, long size, String remotePath, PostActionSpec spec) throws Exception {
        try {
            context.throwIfCancelled();
            context.appendLine("[SSH] 连接 " + server.getServerName() + " ...");
            try (OperationSshSession session = sshClient.connect(server)) {
                context.appendLine("[SSH] 已连接 " + session.getHost());
                context.appendLine("[SFTP] 上传 → " + remotePath + " (" + humanSize(size) + ")");
                try (InputStream in = Files.newInputStream(temp)) {
                    sshClient.sftpPut(session, in, remotePath, new TaskProgressMonitor(context, size));
                }
                context.setProgress(UPLOAD_PROGRESS_CAP);
                context.appendLine("[SFTP] 上传完成");
                executePostAction(context, session, remotePath, spec);
            }
        } finally {
            deleteQuietly(temp);
        }
    }

    private void executePostAction(OperationTaskContext context, OperationSshSession session,
                                   String remotePath, PostActionSpec spec) {
        if ("none".equals(spec.actionLabel)) {
            context.setProgress(100);
            return;
        }
        context.appendLine("[POST] 执行后置: " + spec.actionLabel);
        String command = spec.remoteCommand;
        OperationSshCommandResult result = sshClient.exec(session, command, context::appendLine);
        if (!result.isSuccess()) {
            throw new BaseException("后置动作失败（退出码 " + result.getExitCode() + "）");
        }
        context.setProgress(100);
        context.appendLine("[POST] 后置动作完成");
    }

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

    private PostActionSpec normalizePostAction(String postAction, String postCommand, String remotePath) {
        String action = StringUtils.defaultIfBlank(postAction, "none").trim();
        if ("none".equals(action)) {
            return new PostActionSpec("none", null);
        }
        if ("nginxReload".equals(action)) {
            return new PostActionSpec(action, "sudo nginx -t && sudo nginx -s reload");
        }
        if ("unzipToDist".equals(action)) {
            if (!remotePath.toLowerCase(Locale.ROOT).endsWith(".zip")) {
                throw new BaseException("unzipToDist 仅支持 .zip 文件");
            }
            return new PostActionSpec(action, buildUnzipToDistCommand(remotePath));
        }
        if (action.startsWith("restartService:")) {
            String serviceKey = action.substring("restartService:".length()).trim().toLowerCase(Locale.ROOT);
            if (!deployServiceRegistry.isKnownKey(serviceKey)) {
                throw new BaseException("不支持的 serviceKey: " + serviceKey);
            }
            if (!deployProperties.isEnabled()) {
                throw OperationBizException.deployDisabled();
            }
            String script = deployProperties.getDeployRoot() + "/deploy/linux/moli-service.sh";
            return new PostActionSpec(action,
                    "bash " + OperationSshClient.shellQuote(script) + " " + serviceKey + " restart");
        }
        if ("custom".equals(action)) {
            if (!SecurityUtils.getSubject().isPermitted(PermissionConstants.OPERATION_COMMAND_EXEC)) {
                throw new BaseException("自定义后置命令需 operation:command:exec 权限");
            }
            if (!commandProperties.isEnabled()) {
                throw new BaseException("自定义命令需 ops.command.enabled=true 及 operation:command:exec 权限");
            }
            if (StringUtils.isBlank(postCommand)) {
                throw new BaseException("postAction=custom 时 postCommand 不能为空");
            }
            String cmd = OperationShellGuard.validateCommand(postCommand, commandProperties.getMaxChars());
            return new PostActionSpec("custom", cmd);
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

    private static final class PostActionSpec {
        private final String actionLabel;
        private final String remoteCommand;

        private PostActionSpec(String actionLabel, String remoteCommand) {
            this.actionLabel = actionLabel;
            this.remoteCommand = remoteCommand;
        }
    }

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
