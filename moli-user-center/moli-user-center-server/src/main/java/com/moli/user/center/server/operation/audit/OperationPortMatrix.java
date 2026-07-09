package com.moli.user.center.server.operation.audit;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 与 {@code docs/ops/production-checklist.md} §2 网络与端口表对齐的期望端口矩阵。
 */
public final class OperationPortMatrix {

    private static final List<Entry> ENTRIES = Arrays.asList(
            entry("gateway", "21000", "gateway", "moli-gateway"),
            entry("user-center", "8888", "user-center", "moli-user-center", "user-center-server", "moli-server"),
            entry("order", "8087", "order", "moli-order"),
            entry("knowledge", "8090", "knowledge", "moli-knowledge", "knowledge-server"),
            entry("bi", "1128", "bi", "moli-bi"),
            entry("nacos", "8848", "nacos"),
            entry("mysql", "3306", "mysql"),
            entry("redis", "6379", "redis")
    );

    private OperationPortMatrix() {
    }

    public static PortCheck check(String name, String port) {
        Optional<Entry> entry = resolve(name);
        if (!entry.isPresent()) {
            return new PortCheck(OperationPortMatchStatus.UNMAPPED, null, null, "未在端口矩阵中登记");
        }
        Entry matched = entry.get();
        String actual = normalizePort(port);
        if (actual == null) {
            return new PortCheck(OperationPortMatchStatus.SKIPPED, matched.expectedPort, matched.key,
                    "无端口可比对");
        }
        if (actual.equals(matched.expectedPort)) {
            return new PortCheck(OperationPortMatchStatus.MATCH, matched.expectedPort, matched.key, "与矩阵一致");
        }
        return new PortCheck(OperationPortMatchStatus.MISMATCH, matched.expectedPort, matched.key,
                "期望 " + matched.expectedPort + "，实际 " + actual);
    }

    public static Optional<Entry> resolve(String name) {
        if (StringUtils.isBlank(name)) {
            return Optional.empty();
        }
        String normalized = normalizeName(name);
        for (Entry entry : ENTRIES) {
            if (entry.matches(normalized)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }

    private static Entry entry(String key, String expectedPort, String... aliases) {
        return new Entry(key, expectedPort, aliases);
    }

    private static String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    public static String normalizePort(String port) {
        if (StringUtils.isBlank(port)) {
            return null;
        }
        String trimmed = port.trim();
        if ("-".equals(trimmed)) {
            return null;
        }
        return trimmed;
    }

    public static final class Entry {
        public final String key;
        public final String expectedPort;
        private final List<String> aliases;

        private Entry(String key, String expectedPort, String... aliases) {
            this.key = key;
            this.expectedPort = expectedPort;
            this.aliases = Arrays.asList(aliases);
        }

        boolean matches(String normalizedName) {
            if (key.equals(normalizedName)) {
                return true;
            }
            for (String alias : aliases) {
                if (alias.equals(normalizedName)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static final class PortCheck {
        public final int status;
        public final String expectedPort;
        public final String matrixKey;
        public final String message;

        public PortCheck(int status, String expectedPort, String matrixKey, String message) {
            this.status = status;
            this.expectedPort = expectedPort;
            this.matrixKey = matrixKey;
            this.message = message;
        }
    }
}
