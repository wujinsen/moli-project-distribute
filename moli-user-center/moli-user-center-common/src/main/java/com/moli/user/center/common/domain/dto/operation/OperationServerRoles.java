package com.moli.user.center.common.domain.dto.operation;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 服务器角色字典（台账分类，与 environment 正交）。
 */
public final class OperationServerRoles {

    public static final String APP = "app";
    public static final String DB = "db";
    public static final String CACHE = "cache";
    public static final String MQ = "mq";
    public static final String GATEWAY = "gateway";
    public static final String BASTION = "bastion";
    public static final String MIDDLEWARE = "middleware";
    public static final String OTHER = "other";

    private static final Set<String> ALL = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            APP, DB, CACHE, MQ, GATEWAY, BASTION, MIDDLEWARE, OTHER
    )));

    private OperationServerRoles() {
    }

    public static Set<String> all() {
        return ALL;
    }

    public static boolean isValid(String role) {
        return role != null && ALL.contains(role);
    }
}
