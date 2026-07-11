package com.moli.user.center.server.operation.deploy;

import com.moli.user.center.common.domain.dto.operation.OperationDeployServiceCatalog;
import com.moli.user.center.common.domain.dto.operation.OperationDeployServiceEntry;
import org.junit.After;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OperationDeployServiceCatalogTest {

    @After
    public void restoreDefaults() {
        OperationDeployServiceCatalog.install(null);
    }

    @Test
    public void resolve_known_aliases_by_default() {
        assertEquals("user-center", OperationDeployServiceCatalog.resolveProjectName("moli-server"));
        assertEquals("gateway", OperationDeployServiceCatalog.resolveProjectName("moli-gateway"));
        assertEquals("knowledge", OperationDeployServiceCatalog.resolveProjectName("knowledge-server"));
    }

    @Test
    public void resolve_unknown_returns_null() {
        assertNull(OperationDeployServiceCatalog.resolveProjectName("moli-admin"));
        assertNull(OperationDeployServiceCatalog.resolveProjectName(""));
    }

    @Test
    public void install_custom_service_extends_known_keys() {
        OperationDeployServiceCatalog.install(Collections.singletonList(
                entry("order", "订单", "moli-order", "order")));

        assertTrue(OperationDeployServiceCatalog.isKnownKey("order"));
        assertEquals("order", OperationDeployServiceCatalog.resolveProjectName("moli-order"));
        assertFalse(OperationDeployServiceCatalog.isKnownKey("user-center"));
    }

    @Test
    public void resolve_order_and_bi_aliases_when_configured() {
        OperationDeployServiceCatalog.install(Arrays.asList(
                entry("user-center", "用户中心", "moli-server"),
                entry("order", "订单", "order", "moli-order"),
                entry("bi", "BI", "bi", "moli-bi")
        ));

        assertEquals("order", OperationDeployServiceCatalog.resolveProjectName("moli-order"));
        assertEquals("bi", OperationDeployServiceCatalog.resolveProjectName("moli-bi"));
        assertTrue(OperationDeployServiceCatalog.isKnownKey("order"));
        assertTrue(OperationDeployServiceCatalog.isKnownKey("bi"));
    }

    private static OperationDeployServiceEntry entry(String key, String label, String... aliases) {
        OperationDeployServiceEntry row = new OperationDeployServiceEntry();
        row.setKey(key);
        row.setLabel(label);
        row.setAliases(Arrays.asList(aliases));
        return row;
    }
}

