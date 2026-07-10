package com.moli.user.center.server.operation.guard;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.server.operation.config.OperationUploadProperties;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 上传目标路径校验（SVR-19）：全局前缀 + 服务器级 + allow-any-under。
 */
public final class OperationPathPolicy {

    private static final List<String> DEFAULT_PATH_PRESETS = java.util.Arrays.asList(
            "/opt/moli/frontend/dist/",
            "/opt/moli-project-distribute/moli-user-center/",
            "/opt/moli-project-distribute/moli-gateway/",
            "/opt/moli-project-distribute/moli-knowledge/"
    );

    private OperationPathPolicy() {
    }

    public static String resolveRemotePath(String targetPath, String originalFilename,
                                          OperationServerInfo server,
                                          OperationUploadProperties uploadProperties) {
        String path = OperationShellGuard.validateAbsolutePath(
                normalizeTargetPath(targetPath, originalFilename), "targetPath");
        if (!isAllowed(path, server, uploadProperties)) {
            throw new BaseException("targetPath 不在允许范围内，可配置 ops.upload.allowed-paths、"
                    + "ops.upload.allow-any-under 或服务器 upload_allowed_roots");
        }
        return path;
    }

    private static String normalizeTargetPath(String targetPath, String originalFilename) {
        if (StringUtils.isBlank(targetPath)) {
            throw new BaseException("targetPath 不能为空");
        }
        String path = targetPath.trim().replace('\\', '/');
        if (path.endsWith("/")) {
            String name = StringUtils.isNotBlank(originalFilename)
                    ? Paths.get(originalFilename.replace('\\', '/')).getFileName().toString()
                    : "upload.bin";
            path = path + name;
        }
        return path;
    }

    public static boolean isAllowed(String absolutePath, OperationServerInfo server,
                                    OperationUploadProperties uploadProperties) {
        List<String> roots = collectAllowedRoots(server, uploadProperties);
        return roots.stream().anyMatch(absolutePath::startsWith);
    }

    public static List<String> collectAllowedRoots(OperationServerInfo server,
                                                   OperationUploadProperties uploadProperties) {
        Set<String> roots = new LinkedHashSet<>();
        if (uploadProperties != null) {
            uploadProperties.getAllowedPaths().stream()
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .forEach(roots::add);
            uploadProperties.getAllowAnyUnder().stream()
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .map(p -> p.endsWith("/") ? p : p + "/")
                    .forEach(roots::add);
        }
        if (server != null && StringUtils.isNotBlank(server.getUploadAllowedRoots())) {
            for (String part : server.getUploadAllowedRoots().split("[,;\\n]")) {
                String p = part.trim();
                if (StringUtils.isNotBlank(p)) {
                    roots.add(p.endsWith("/") ? p : p + "/");
                }
            }
        }
        return new ArrayList<>(roots);
    }

    public static List<String> pathPresets(OperationServerInfo server,
                                           OperationUploadProperties uploadProperties) {
        Set<String> presets = new LinkedHashSet<>(DEFAULT_PATH_PRESETS);
        presets.addAll(collectAllowedRoots(server, uploadProperties));
        return presets.stream().sorted().collect(Collectors.toList());
    }
}
