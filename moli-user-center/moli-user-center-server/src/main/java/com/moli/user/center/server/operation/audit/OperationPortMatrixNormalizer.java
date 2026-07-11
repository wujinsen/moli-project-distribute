package com.moli.user.center.server.operation.audit;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * 端口矩阵名称/端口归一化（SVR-21）。
 */
public final class OperationPortMatrixNormalizer {

    private OperationPortMatrixNormalizer() {
    }

    public static String normalizeName(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }
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

    public static String normalizeAliasToken(String alias) {
        return normalizeName(alias);
    }
}
