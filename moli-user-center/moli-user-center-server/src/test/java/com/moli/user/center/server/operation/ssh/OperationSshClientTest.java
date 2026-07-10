package com.moli.user.center.server.operation.ssh;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OperationSshClientTest {

    @Test
    public void shellQuote_wraps_simple_path() {
        assertEquals("'/opt/moli/app.jar'", OperationSshClient.shellQuote("/opt/moli/app.jar"));
    }

    @Test
    public void shellQuote_escapes_single_quotes() {
        assertEquals("'it'\\''s'", OperationSshClient.shellQuote("it's"));
    }

    @Test
    public void shellQuote_null_becomes_empty_quoted() {
        assertEquals("''", OperationSshClient.shellQuote(null));
    }
}
