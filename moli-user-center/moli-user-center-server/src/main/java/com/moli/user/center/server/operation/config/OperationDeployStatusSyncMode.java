package com.moli.user.center.server.operation.config;

/**
 * {@link OperationDeployProperties#getStatusSyncMode()} 取值。
 */
public final class OperationDeployStatusSyncMode {

    /** 有 serverId 时走 SSH status；无 serverId 时在 Linux 本机查脚本。 */
    public static final String SSH = "ssh";
    /** 始终本机 moli-service.sh（忽略 serverId）。 */
    public static final String LOCAL = "local";
    /** 不同步 deploy_running。 */
    public static final String OFF = "off";

    private OperationDeployStatusSyncMode() {
    }

    public static String normalize(String mode) {
        if (mode == null) {
            return SSH;
        }
        String normalized = mode.trim().toLowerCase();
        if (LOCAL.equals(normalized) || OFF.equals(normalized)) {
            return normalized;
        }
        return SSH;
    }
}
