package com.moli.user.center.server.operation.audit;

/**
 * 台账端口与矩阵比对结果。
 */
public final class OperationPortMatrixPortCheck {

    public final int status;
    public final String expectedPort;
    public final String matrixKey;
    public final String message;

    public OperationPortMatrixPortCheck(int status, String expectedPort, String matrixKey, String message) {
        this.status = status;
        this.expectedPort = expectedPort;
        this.matrixKey = matrixKey;
        this.message = message;
    }
}
