package com.moli.user.center.server.operation.audit;

import org.junit.Assert;
import org.junit.Test;

public class OperationPortMatrixTest {

    @Test
    public void mysql_port_match() {
        OperationPortMatrix.PortCheck check = OperationPortMatrix.check("MySQL", "3306");
        Assert.assertEquals(OperationPortMatchStatus.MATCH, check.status);
        Assert.assertEquals("3306", check.expectedPort);
    }

    @Test
    public void moli_server_alias_mismatch() {
        OperationPortMatrix.PortCheck check = OperationPortMatrix.check("moli-server", "9080");
        Assert.assertEquals(OperationPortMatchStatus.MISMATCH, check.status);
        Assert.assertEquals("8888", check.expectedPort);
    }

    @Test
    public void unknown_name_unmapped() {
        OperationPortMatrix.PortCheck check = OperationPortMatrix.check("moli-admin", "9528");
        Assert.assertEquals(OperationPortMatchStatus.UNMAPPED, check.status);
    }

    @Test
    public void blank_port_skipped() {
        OperationPortMatrix.PortCheck check = OperationPortMatrix.check("Redis", "-");
        Assert.assertEquals(OperationPortMatchStatus.SKIPPED, check.status);
    }
}
