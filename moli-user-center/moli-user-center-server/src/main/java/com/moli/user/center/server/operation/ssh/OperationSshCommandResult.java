package com.moli.user.center.server.operation.ssh;

/**
 * 远程命令执行结果。
 */
public class OperationSshCommandResult {

    private final int exitCode;
    private final String output;

    public OperationSshCommandResult(int exitCode, String output) {
        this.exitCode = exitCode;
        this.output = output;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getOutput() {
        return output;
    }

    public boolean isSuccess() {
        return exitCode == 0;
    }
}
