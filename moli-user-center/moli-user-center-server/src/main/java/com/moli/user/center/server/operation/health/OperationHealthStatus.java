package com.moli.user.center.server.operation.health;

/**
 * 运维健康探测状态（operation_server_info / operation_component_deploy_info.status）
 */
public final class OperationHealthStatus {

    public static final int UNKNOWN = 0;
    public static final int UP = 1;
    public static final int DOWN = 2;
    /** 缺少 IP/端口，未执行探测 */
    public static final int SKIPPED = 3;

    private OperationHealthStatus() {
    }
}
