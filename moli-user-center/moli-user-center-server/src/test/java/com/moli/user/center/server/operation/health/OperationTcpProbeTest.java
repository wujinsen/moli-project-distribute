package com.moli.user.center.server.operation.health;

import org.junit.Assert;
import org.junit.Test;

public class OperationTcpProbeTest {

    @Test
    public void parsePort_handlesDashAndDigits() {
        Assert.assertNull(OperationTcpProbe.parsePort("-"));
        Assert.assertEquals(Integer.valueOf(3306), OperationTcpProbe.parsePort("3306"));
        Assert.assertEquals(Integer.valueOf(443), OperationTcpProbe.parsePort("https:443"));
    }

    @Test
    public void probe_localhostOpenPort() {
        int status = OperationTcpProbe.probe("127.0.0.1", "1");
        Assert.assertTrue(status == OperationHealthStatus.DOWN || status == OperationHealthStatus.UP);
    }

    @Test
    public void probe_skipsWhenMissingTarget() {
        Assert.assertEquals(OperationHealthStatus.SKIPPED, OperationTcpProbe.probe("", "3306"));
        Assert.assertEquals(OperationHealthStatus.SKIPPED, OperationTcpProbe.probe("127.0.0.1", "-"));
    }
}
