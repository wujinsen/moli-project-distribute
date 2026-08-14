package com.moli.user.center.server.operation.audit;

/**
 * 台账端口与 {@link OperationPortMatrix} 比对结果。
 */
public final class OperationPortMatchStatus {

    public static final int UNMAPPED = 0;
    public static final int MATCH = 1;
    public static final int MISMATCH = 2;
    public static final int SKIPPED = 3;

    private OperationPortMatchStatus() {
    }
}
