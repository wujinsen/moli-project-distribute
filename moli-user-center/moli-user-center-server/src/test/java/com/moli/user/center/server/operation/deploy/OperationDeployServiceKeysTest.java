package com.moli.user.center.server.operation.deploy;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OperationDeployServiceKeysTest {

    @Test
    public void resolve_known_aliases() {
        assertEquals("user-center", OperationDeployServiceKeys.resolve("moli-server"));
        assertEquals("gateway", OperationDeployServiceKeys.resolve("moli-gateway"));
        assertEquals("knowledge", OperationDeployServiceKeys.resolve("knowledge-server"));
    }

    @Test
    public void resolve_unknown_returns_null() {
        assertNull(OperationDeployServiceKeys.resolve("moli-admin"));
        assertNull(OperationDeployServiceKeys.resolve(""));
        assertNull(OperationDeployServiceKeys.resolve(null));
    }
}
