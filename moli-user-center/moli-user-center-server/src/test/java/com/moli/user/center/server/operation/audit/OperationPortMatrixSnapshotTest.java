package com.moli.user.center.server.operation.audit;

import org.junit.Assert;
import org.junit.Test;

public class OperationPortMatrixSnapshotTest {

    private final OperationPortMatrixSnapshot snapshot = OperationPortMatrixDefaults.snapshot();

    @Test
    public void mysql_port_match() {
        OperationPortMatrixPortCheck check = snapshot.check("MySQL", "3306");
        Assert.assertEquals(OperationPortMatchStatus.MATCH, check.status);
        Assert.assertEquals("3306", check.expectedPort);
    }

    @Test
    public void moli_server_alias_mismatch() {
        OperationPortMatrixPortCheck check = snapshot.check("moli-server", "9080");
        Assert.assertEquals(OperationPortMatchStatus.MISMATCH, check.status);
        Assert.assertEquals("8888", check.expectedPort);
    }

    @Test
    public void unknown_name_unmapped() {
        OperationPortMatrixPortCheck check = snapshot.check("moli-admin", "9528");
        Assert.assertEquals(OperationPortMatchStatus.UNMAPPED, check.status);
    }

    @Test
    public void blank_port_skipped() {
        OperationPortMatrixPortCheck check = snapshot.check("Redis", "-");
        Assert.assertEquals(OperationPortMatchStatus.SKIPPED, check.status);
    }

    @Test
    public void audit_entries_contains_seed_rows() {
        Assert.assertFalse(snapshot.auditEntries().isEmpty());
        Assert.assertTrue(snapshot.isUsingDefaults());
    }
}
