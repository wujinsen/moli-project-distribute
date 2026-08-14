package com.moli.user.center.server.operation.audit;

import com.moli.user.center.common.domain.entity.OperationPortMatrixAliasInfo;
import com.moli.user.center.common.domain.entity.OperationPortMatrixInfo;
import com.moli.user.center.server.operation.mapper.OperationPortMatrixAliasMapper;
import com.moli.user.center.server.operation.mapper.OperationPortMatrixMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationPortMatrixProviderTest {

    @InjectMocks
    private OperationPortMatrixProvider provider;

    @Mock
    private OperationPortMatrixMapper operationPortMatrixMapper;
    @Mock
    private OperationPortMatrixAliasMapper operationPortMatrixAliasMapper;

    @Before
    public void loadDbMatrix() {
        OperationPortMatrixInfo userCenter = new OperationPortMatrixInfo();
        userCenter.setId(502L);
        userCenter.setMatrixKey("user-center");
        userCenter.setExpectedPort("9080");
        userCenter.setSortOrder(20);
        userCenter.setEnabled(Boolean.TRUE);
        userCenter.setSource("ops-console");
        when(operationPortMatrixMapper.selectList(any())).thenReturn(Collections.singletonList(userCenter));

        OperationPortMatrixAliasInfo alias = new OperationPortMatrixAliasInfo();
        alias.setMatrixId(502L);
        alias.setAlias("moli-server");
        when(operationPortMatrixAliasMapper.selectList(any())).thenReturn(Collections.singletonList(alias));

        provider.refresh();
    }

    @Test
    public void refresh_uses_db_enabled_rows() {
        assertFalse(provider.isUsingDefaults());
        OperationPortMatrixPortCheck check = provider.check("moli-server", "9080");
        assertEquals(OperationPortMatchStatus.MATCH, check.status);
        assertEquals("9080", check.expectedPort);
    }

    @Test
    public void refresh_empty_table_falls_back_to_defaults() {
        when(operationPortMatrixMapper.selectList(any())).thenReturn(Collections.emptyList());
        provider.refresh();
        assertTrue(provider.isUsingDefaults());
        OperationPortMatrixPortCheck check = provider.check("MySQL", "3306");
        assertEquals(OperationPortMatchStatus.MATCH, check.status);
    }

    @Test
    public void audit_entries_reflect_db_source() {
        assertEquals("ops-console", provider.auditEntries().get(0).getSource());
    }
}
