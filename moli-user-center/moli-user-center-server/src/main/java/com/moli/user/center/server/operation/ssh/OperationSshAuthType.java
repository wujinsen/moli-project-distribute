package com.moli.user.center.server.operation.ssh;

/**
 * SSH 认证方式。
 */
public final class OperationSshAuthType {

    /** 私钥认证。 */
    public static final int PRIVATE_KEY = 1;
    /** 密码认证。 */
    public static final int PASSWORD = 2;

    private OperationSshAuthType() {
    }
}
