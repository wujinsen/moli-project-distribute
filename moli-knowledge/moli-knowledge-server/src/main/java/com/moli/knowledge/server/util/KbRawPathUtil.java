package com.moli.knowledge.server.util;

import com.moli.common.exception.BaseException;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * raw 根目录解析与路径安全（T20a · 与 Ingest raw-tree / Asset 口径一致）。
 */
public final class KbRawPathUtil {

    private static final Set<String> ALLOWED_RAW_EXTENSIONS = new HashSet<>(
            Arrays.asList("md", "markdown", "txt"));

    private KbRawPathUtil() {
    }

    public static Path resolveRawRoot(String rawRootConfig) {
        Path root = Paths.get(StringUtils.defaultIfBlank(rawRootConfig, "moli-knowledge/kb/raw"));
        if (!root.isAbsolute()) {
            root = Paths.get(System.getProperty("user.dir")).resolve(root);
        }
        return root.normalize();
    }

    /** 规范化 prefix：去首尾 {@code /}，禁止 {@code ..}、绝对路径、盘符。 */
    public static String normalizePrefix(String prefix) {
        if (StringUtils.isBlank(prefix)) {
            throw new BaseException("prefix 不能为空");
        }
        String p = prefix.trim().replace('\\', '/');
        while (p.startsWith("/")) {
            p = p.substring(1);
        }
        while (p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        if (p.isEmpty()) {
            throw new BaseException("prefix 不能为空");
        }
        if (p.contains("..") || p.contains(":")) {
            throw new BaseException("非法 prefix: " + prefix);
        }
        return p;
    }

    /** 解析相对 raw 根的路径并校验在根内（防目录穿越）。blank relative → root。 */
    public static Path normalizeUnder(Path root, String relative) {
        Path target;
        if (StringUtils.isBlank(relative)) {
            target = root;
        } else {
            String rel = relative.trim().replace('\\', '/');
            if (rel.startsWith("/") || rel.contains("..") || rel.contains(":")) {
                throw new BaseException("非法路径（越权）: " + relative);
            }
            target = root.resolve(rel).normalize();
        }
        if (!target.startsWith(root)) {
            throw new BaseException("非法路径（越权）: " + relative);
        }
        return target;
    }

    public static String sanitizeFileName(String originalName) {
        if (StringUtils.isBlank(originalName)) {
            throw new BaseException("文件名不能为空");
        }
        String name = Paths.get(originalName.trim()).getFileName().toString();
        if (name.isEmpty() || name.contains("..") || name.contains("/") || name.contains("\\")) {
            throw new BaseException("非法文件名: " + originalName);
        }
        return name;
    }

    public static void assertAllowedRawExtension(String fileName) {
        String ext = extension(fileName);
        if (!ALLOWED_RAW_EXTENSIONS.contains(ext)) {
            throw new BaseException("不支持的文件类型: " + fileName + "（仅 .md / .markdown / .txt）");
        }
    }

    public static String extension(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".note.md")) {
            return "md";
        }
        int dot = lower.lastIndexOf('.');
        if (dot < 0 || dot == lower.length() - 1) {
            return "";
        }
        return lower.substring(dot + 1);
    }

    public static String renameWithSuffix(String fileName, int n) {
        String base = fileName;
        String ext = "";
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) {
            base = fileName.substring(0, dot);
            ext = fileName.substring(dot);
        }
        return base + "-" + n + ext;
    }
}
