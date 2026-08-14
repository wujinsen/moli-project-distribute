package com.moli.user.center.server.operation.guard;

import com.moli.common.exception.BaseException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 远程 shell 受控开放校验（SVR-18）：拦截高危命令，允许运维组合（; && |）。
 */
public final class OperationShellGuard {

    private static final int DEFAULT_MAX_CHARS = 8192;

    private static final List<Pattern> DANGEROUS_PATTERNS = Arrays.asList(
            Pattern.compile("rm\\s+-[a-zA-Z]*f[a-zA-Z]*\\s+/", Pattern.CASE_INSENSITIVE),
            Pattern.compile("rm\\s+-[a-zA-Z]*f[a-zA-Z]*\\s+/\\s", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bmkfs\\.", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bdd\\s+if=", Pattern.CASE_INSENSITIVE),
            Pattern.compile(">\\s*/etc/", Pattern.CASE_INSENSITIVE),
            Pattern.compile(">>\\s*/etc/", Pattern.CASE_INSENSITIVE),
            Pattern.compile("curl[^\\n|]*\\|\\s*ba?sh", Pattern.CASE_INSENSITIVE),
            Pattern.compile("wget[^\\n|]*\\|\\s*ba?sh", Pattern.CASE_INSENSITIVE),
            Pattern.compile("wget[^\\n|]*\\|\\s*sh\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile(":\\(\\)\\s*\\{\\s*:\\|:&\\s*\\};:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bshutdown\\s+-[hHrR]", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\breboot\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\binit\\s+0\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bhalt\\b", Pattern.CASE_INSENSITIVE)
    );

    private OperationShellGuard() {
    }

    /**
     * 校验用户输入的 shell 命令；不通过则抛 BaseException。
     */
    public static String validateCommand(String command, int maxChars) {
        if (StringUtils.isBlank(command)) {
            throw new BaseException("命令不能为空");
        }
        String trimmed = command.trim();
        int limit = maxChars > 0 ? maxChars : DEFAULT_MAX_CHARS;
        if (trimmed.length() > limit) {
            throw new BaseException("命令超过长度上限 " + limit + " 字符");
        }
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '\0' || (c < 32 && c != '\n' && c != '\r' && c != '\t')) {
                throw new BaseException("命令包含非法控制字符");
            }
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(lower).find()) {
                throw new BaseException("命令包含高危操作，已被拦截");
            }
        }
        return trimmed;
    }

    public static String validateCommand(String command) {
        return validateCommand(command, DEFAULT_MAX_CHARS);
    }

    /**
     * 校验工作目录或上传目标路径（绝对路径、禁止 ..）。
     */
    public static String validateAbsolutePath(String path, String fieldName) {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        String normalized = path.trim().replace('\\', '/');
        if (!normalized.startsWith("/")) {
            throw new BaseException(fieldName + " 必须为绝对路径");
        }
        if (normalized.contains("..")) {
            throw new BaseException(fieldName + " 不允许包含 ..");
        }
        return normalized.endsWith("/") && normalized.length() > 1
                ? normalized.substring(0, normalized.length() - 1)
                : normalized;
    }

    /**
     * 构建带可选工作目录的远程执行命令。
     */
    public static String buildRemoteCommand(String command, String workDir) {
        String validated = validateCommand(command);
        String dir = validateAbsolutePath(workDir, "workDir");
        if (dir == null) {
            return validated;
        }
        return "cd " + com.moli.user.center.server.operation.ssh.OperationSshClient.shellQuote(dir)
                + " && " + validated;
    }

    public static String abbreviateForAudit(String command, int maxLen) {
        if (command == null) {
            return "";
        }
        String oneLine = command.replace('\n', ' ').replace('\r', ' ').trim();
        return StringUtils.abbreviate(oneLine, maxLen);
    }
}
