package com.moli.user.center.server.operation.audit;

import java.util.List;
import java.util.Optional;

/**
 * @deprecated 使用 {@link OperationPortMatrixProvider}；保留供历史测试与回退默认对照。
 */
@Deprecated
public final class OperationPortMatrix {

    private OperationPortMatrix() {
    }

    public static PortCheck check(String name, String port) {
        return toLegacy(OperationPortMatrixDefaults.snapshot().check(name, port));
    }

    public static Optional<Entry> resolve(String name) {
        return OperationPortMatrixDefaults.snapshot().resolve(name)
                .map(row -> new Entry(row.matrixKey, row.expectedPort, aliasArray(row)));
    }

    public static List<Entry> entries() {
        List<Entry> list = new java.util.ArrayList<>();
        for (OperationPortMatrixSnapshot.Row row : OperationPortMatrixDefaults.defaultRows()) {
            list.add(new Entry(row.matrixKey, row.expectedPort, aliasArray(row)));
        }
        return list;
    }

    public static String normalizePort(String port) {
        return OperationPortMatrixNormalizer.normalizePort(port);
    }

    private static String[] aliasArray(OperationPortMatrixSnapshot.Row row) {
        return new String[]{row.matrixKey};
    }

    private static PortCheck toLegacy(OperationPortMatrixPortCheck check) {
        return new PortCheck(check.status, check.expectedPort, check.matrixKey, check.message);
    }

    public static final class Entry {
        public final String key;
        public final String expectedPort;
        private final List<String> aliases;

        private Entry(String key, String expectedPort, String... aliases) {
            this.key = key;
            this.expectedPort = expectedPort;
            this.aliases = java.util.Arrays.asList(aliases);
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
