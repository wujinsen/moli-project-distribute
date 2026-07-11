package com.moli.user.center.server.operation.audit;

import java.util.Arrays;
import java.util.List;

/**
 * 内置默认矩阵（与历史 {@link OperationPortMatrix} 硬编码一致）。
 */
public final class OperationPortMatrixDefaults {

    private static final String SOURCE = "java-default";

    private OperationPortMatrixDefaults() {
    }

    public static OperationPortMatrixSnapshot snapshot() {
        return OperationPortMatrixSnapshot.of(defaultRows(), true);
    }

    static List<OperationPortMatrixSnapshot.Row> defaultRows() {
        return Arrays.asList(
                row("gateway", "21000", 10, "gateway", "moli-gateway"),
                row("user-center", "8888", 20, "user-center", "moli-user-center", "user-center-server", "moli-server"),
                row("order", "8087", 30, "order", "moli-order"),
                row("knowledge", "8090", 40, "knowledge", "moli-knowledge", "knowledge-server"),
                row("bi", "1128", 50, "bi", "moli-bi"),
                row("nacos", "8848", 60, "nacos"),
                row("mysql", "3306", 70, "mysql"),
                row("redis", "6379", 80, "redis")
        );
    }

    private static OperationPortMatrixSnapshot.Row row(String key, String port, int sort, String... aliases) {
        return new OperationPortMatrixSnapshot.Row(key, port, SOURCE, sort,
                aliases.length > 0 ? Arrays.asList(aliases) : null);
    }
}
