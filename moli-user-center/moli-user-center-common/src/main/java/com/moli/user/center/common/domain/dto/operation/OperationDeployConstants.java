package com.moli.user.center.common.domain.dto.operation;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 部署中心 action / postAction 白名单；serviceKey 见 {@link OperationDeployServiceCatalog}。
 */
public final class OperationDeployConstants {

    public static final Set<String> TASK_ACTIONS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("start", "stop", "restart")));

    public static final Set<String> READ_ONLY_ACTIONS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("status", "logs")));

    public static final Set<String> POST_ACTIONS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("none", "nginxReload", "unzipToDist", "custom")));

    private OperationDeployConstants() {
    }

    public static String normalizeServiceKey(String serviceKey) {
        return StringUtils.isBlank(serviceKey) ? null : serviceKey.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeAction(String action) {
        return StringUtils.isBlank(action) ? null : action.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isValidPostAction(String postAction) {
        if (StringUtils.isBlank(postAction)) {
            return false;
        }
        String action = postAction.trim();
        if (POST_ACTIONS.contains(action)) {
            return true;
        }
        if (action.startsWith("restartService:")) {
            String key = normalizeServiceKey(action.substring("restartService:".length()));
            return key != null && OperationDeployServiceCatalog.isKnownKey(key);
        }
        return false;
    }
}
