package com.moli.knowledge.server.util;

import com.moli.common.exception.BaseException;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 解析 kb 脚本/目录相对路径：兼容 IDE（cwd=monorepo 根）与 {@code mvn spring-boot:run}（cwd=server 模块）。
 */
public final class KbRepoPathUtil {

    private KbRepoPathUtil() {
    }

    public static Path resolveExisting(String configuredPath, String label) {
        if (StringUtils.isBlank(configuredPath)) {
            throw new BaseException(label + " 路径未配置");
        }
        Path configured = Paths.get(configuredPath.trim());
        if (configured.isAbsolute()) {
            if (Files.exists(configured)) {
                return configured.normalize();
            }
            throw new BaseException(label + " 不存在: " + configured);
        }

        Path cwd = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        List<Path> bases = Arrays.asList(cwd, cwd.resolve("moli-knowledge-server"));
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(configuredPath.trim());
        if (configuredPath.contains("../kb/")) {
            candidates.add(configuredPath.replace("../kb/", "moli-knowledge/kb/"));
        }
        if (configuredPath.startsWith("moli-knowledge/kb/")) {
            candidates.add(configuredPath.replace("moli-knowledge/kb/", "../kb/"));
        }

        List<String> tried = new ArrayList<>();
        for (String candidate : candidates) {
            for (Path base : bases) {
                Path resolved = base.resolve(candidate).normalize();
                tried.add(resolved.toString());
                if (Files.exists(resolved)) {
                    return resolved;
                }
            }
        }
        throw new BaseException(label + " 不存在: " + configuredPath + " (cwd=" + cwd + "; tried=" + tried + ")");
    }
}
