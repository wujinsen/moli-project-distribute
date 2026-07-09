package com.moli.user.center.server.operation.deploy;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 项目名 → moli-service.sh serviceKey 映射（与前端 operationPort.ts 一致）。
 */
public final class OperationDeployServiceKeys {

    private static final Map<String, String> ALIASES = new HashMap<>();

    static {
        alias("gateway", "gateway");
        alias("moli-gateway", "gateway");
        alias("user-center", "user-center");
        alias("moli-user-center", "user-center");
        alias("user-center-server", "user-center");
        alias("moli-server", "user-center");
        alias("knowledge", "knowledge");
        alias("moli-knowledge", "knowledge");
        alias("knowledge-server", "knowledge");
    }

    private OperationDeployServiceKeys() {
    }

    private static void alias(String name, String serviceKey) {
        ALIASES.put(normalize(name), serviceKey);
    }

    public static String resolve(String projectName) {
        if (StringUtils.isBlank(projectName)) {
            return null;
        }
        return ALIASES.get(normalize(projectName));
    }

    private static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }
}
