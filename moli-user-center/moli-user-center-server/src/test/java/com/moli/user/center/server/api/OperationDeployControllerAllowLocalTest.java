package com.moli.user.center.server.api;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.dto.operation.OperationDeployTaskRequest;
import com.moli.user.center.common.domain.vo.OperationDeployStatusVo;
import com.moli.user.center.server.operation.controller.OperationDeployController;
import com.moli.user.center.server.operation.service.OperationDeployPresetService;
import com.moli.user.center.server.operation.service.OperationDeployService;
import com.moli.user.center.server.operation.service.OperationRemoteDeployService;
import com.moli.user.center.server.operation.support.OperationBizException;
import com.moli.user.center.server.operation.support.OperationDeployLocalPolicy;
import com.moli.user.center.server.operation.support.OperationDtoValidationSupport;
import com.moli.user.center.server.testsupport.AbstractApiTest;
import com.moli.user.center.server.testsupport.ControllerTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationDeployControllerAllowLocalTest extends AbstractApiTest {

    @InjectMocks
    private OperationDeployController deployController;

    @Mock
    private OperationDeployService operationDeployService;
    @Mock
    private OperationRemoteDeployService operationRemoteDeployService;
    @Mock
    private OperationDeployPresetService operationDeployPresetService;
    @Mock
    private OperationDtoValidationSupport dtoValidation;
    @Mock
    private OperationDeployLocalPolicy deployLocalPolicy;

    @Before
    public void allowLocalByDefault() {
        doNothing().when(deployLocalPolicy).requireAllowLocal();
    }

    @Test
    public void GET_status_without_serverId_checks_allow_local() {
        when(operationDeployService.status("user-center")).thenReturn(new OperationDeployStatusVo());
        ControllerTestSupport.assertSuccess(deployController.status("user-center", null));
        verify(deployLocalPolicy).requireAllowLocal();
        verify(operationRemoteDeployService, never()).executeRemoteReadOnly(
                any(), any(), any(), any());
    }

    @Test
    public void GET_status_without_serverId_rejects_when_allow_local_disabled() {
        doThrow(OperationBizException.localDeployDisabled()).when(deployLocalPolicy).requireAllowLocal();
        try {
            deployController.status("user-center", null);
        } catch (BaseException ex) {
            assertEquals(Integer.valueOf(OperationBizException.CODE_LOCAL_DEPLOY_DISABLED), ex.getErrorCode());
            verify(operationDeployService, never()).status(any());
            return;
        }
        throw new AssertionError("expected local deploy disabled");
    }

    @Test
    public void GET_status_with_serverId_skips_allow_local() {
        OperationDeployStatusVo vo = new OperationDeployStatusVo();
        vo.setRunning(true);
        when(operationRemoteDeployService.executeRemoteReadOnly(204L, "gateway", "status", null))
                .thenReturn(vo);
        ControllerTestSupport.assertSuccess(deployController.status("gateway", 204L));
        verify(deployLocalPolicy, never()).requireAllowLocal();
        verify(operationDeployService, never()).status(any());
    }

    @Test
    public void POST_execute_without_serverId_checks_allow_local() {
        OperationDeployStatusVo vo = new OperationDeployStatusVo();
        vo.setRunning(true);
        when(operationDeployService.execute("user-center", "restart", null)).thenReturn(vo);
        ControllerTestSupport.assertSuccess(deployController.execute("user-center", "restart", null, null));
        verify(deployLocalPolicy).requireAllowLocal();
    }

    @Test
    public void POST_execute_without_serverId_rejects_when_allow_local_disabled() {
        doThrow(OperationBizException.localDeployDisabled()).when(deployLocalPolicy).requireAllowLocal();
        try {
            deployController.execute("user-center", "restart", null, null);
        } catch (BaseException ex) {
            assertEquals(Integer.valueOf(OperationBizException.CODE_LOCAL_DEPLOY_DISABLED), ex.getErrorCode());
            verify(operationDeployService, never()).execute(any(), any(), any());
            return;
        }
        throw new AssertionError("expected local deploy disabled");
    }

    @Test
    public void POST_execute_with_serverId_skips_allow_local() {
        OperationDeployStatusVo vo = new OperationDeployStatusVo();
        when(operationRemoteDeployService.executeRemoteReadOnly(204L, "knowledge", "status", "extra"))
                .thenReturn(vo);
        ControllerTestSupport.assertSuccess(
                deployController.execute("knowledge", "status", "extra", 204L));
        verify(deployLocalPolicy, never()).requireAllowLocal();
        verify(operationDeployService, never()).execute(any(), any(), any());
    }

    @Test
    public void POST_createTask_without_serverId_delegates_to_remote_service() {
        OperationDeployTaskRequest req = new OperationDeployTaskRequest();
        req.setServiceKey("user-center");
        req.setAction("restart");
        when(dtoValidation.deployTask("user-center", "restart", null, null)).thenReturn(req);
        when(operationRemoteDeployService.createDeployTask(req)).thenReturn(42L);
        ControllerTestSupport.assertSuccess(deployController.createTask("user-center", "restart", null, null));
        verify(deployLocalPolicy, never()).requireAllowLocal();
        verify(operationRemoteDeployService).createDeployTask(req);
    }

    @Test
    public void POST_createTask_without_serverId_propagates_local_disabled_from_service() {
        OperationDeployTaskRequest req = new OperationDeployTaskRequest();
        req.setServiceKey("user-center");
        req.setAction("restart");
        when(dtoValidation.deployTask("user-center", "restart", null, null)).thenReturn(req);
        when(operationRemoteDeployService.createDeployTask(req))
                .thenThrow(OperationBizException.localDeployDisabled());
        try {
            deployController.createTask("user-center", "restart", null, null);
        } catch (BaseException ex) {
            assertEquals(Integer.valueOf(OperationBizException.CODE_LOCAL_DEPLOY_DISABLED), ex.getErrorCode());
            return;
        }
        throw new AssertionError("expected local deploy disabled");
    }

    @Test
    public void POST_createTask_with_serverId_skips_allow_local() {
        OperationDeployTaskRequest req = new OperationDeployTaskRequest();
        req.setServiceKey("gateway");
        req.setAction("stop");
        req.setServerId(204L);
        when(dtoValidation.deployTask("gateway", "stop", 204L, null)).thenReturn(req);
        when(operationRemoteDeployService.createDeployTask(req)).thenReturn(99L);
        ControllerTestSupport.assertSuccess(deployController.createTask("gateway", "stop", 204L, null));
        verify(deployLocalPolicy, never()).requireAllowLocal();
        verify(operationRemoteDeployService).createDeployTask(eq(req));
    }
}
