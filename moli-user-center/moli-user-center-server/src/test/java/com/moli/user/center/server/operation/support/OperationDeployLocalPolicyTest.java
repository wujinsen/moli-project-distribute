package com.moli.user.center.server.operation.support;

import com.moli.common.exception.BaseException;
import com.moli.user.center.server.operation.config.OperationDeployProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationDeployLocalPolicyTest {

    @InjectMocks
    private OperationDeployLocalPolicy deployLocalPolicy;

    @Mock
    private OperationDeployProperties deployProperties;

    @Before
    public void defaultsToDisabled() {
        when(deployProperties.isAllowLocal()).thenReturn(false);
    }

    @Test
    public void requireAllowLocalWhenNoServer_skips_when_server_present() {
        deployLocalPolicy.requireAllowLocalWhenNoServer(204L);
    }

    @Test
    public void requireAllowLocalWhenNoServer_rejects_when_local_disabled() {
        try {
            deployLocalPolicy.requireAllowLocalWhenNoServer(null);
        } catch (BaseException ex) {
            assertEquals(Integer.valueOf(OperationBizException.CODE_LOCAL_DEPLOY_DISABLED), ex.getErrorCode());
            return;
        }
        throw new AssertionError("expected local deploy disabled");
    }

    @Test
    public void requireAllowLocal_rejects_when_disabled() {
        try {
            deployLocalPolicy.requireAllowLocal();
        } catch (BaseException ex) {
            assertEquals(Integer.valueOf(OperationBizException.CODE_LOCAL_DEPLOY_DISABLED), ex.getErrorCode());
            return;
        }
        throw new AssertionError("expected local deploy disabled");
    }

    @Test
    public void requireAllowLocal_allows_when_enabled() {
        when(deployProperties.isAllowLocal()).thenReturn(true);
        deployLocalPolicy.requireAllowLocalWhenNoServer(null);
    }
}
