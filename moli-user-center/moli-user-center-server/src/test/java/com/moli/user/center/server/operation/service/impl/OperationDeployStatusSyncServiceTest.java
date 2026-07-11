package com.moli.user.center.server.operation.service.impl;

import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.vo.OperationDeployStatusVo;
import com.moli.user.center.server.operation.config.OperationDeployProperties;
import com.moli.user.center.server.operation.config.OperationDeployStatusSyncMode;
import com.moli.user.center.server.operation.deploy.OperationDeployServiceRegistry;
import com.moli.user.center.server.operation.service.OperationDeployService;
import com.moli.user.center.server.operation.service.OperationRemoteDeployService;
import com.moli.user.center.server.operation.support.OperationHostEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationDeployStatusSyncServiceTest {

    @InjectMocks
    private OperationDeployStatusSyncServiceImpl statusSyncService;

    @Mock
    private OperationDeployProperties deployProperties;
    @Mock
    private OperationDeployService operationDeployService;
    @Mock
    private OperationRemoteDeployService operationRemoteDeployService;
    @Mock
    private OperationDeployServiceRegistry deployServiceRegistry;
    @Mock
    private OperationHostEnvironment hostEnvironment;

    @Before
    public void useSshMode() {
        when(deployProperties.getStatusSyncMode()).thenReturn(OperationDeployStatusSyncMode.SSH);
        when(hostEnvironment.isLocalLinux()).thenReturn(true);
        when(deployServiceRegistry.resolveProjectName("moli-server")).thenReturn("user-center");
        when(deployServiceRegistry.resolveProjectName("moli-gateway")).thenReturn("gateway");
        when(deployServiceRegistry.resolveProjectName("knowledge-server")).thenReturn("knowledge");
        when(deployServiceRegistry.resolveProjectName("moli-admin")).thenReturn(null);
    }

    @Test
    public void syncProject_skips_unknown_project_name() {
        OperationProjectDeployInfo project = project("moli-admin", 204L);
        assertFalse(statusSyncService.syncProject(project));
        verify(operationRemoteDeployService, never()).executeRemoteReadOnly(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void syncProject_skips_when_mode_off() {
        when(deployProperties.getStatusSyncMode()).thenReturn(OperationDeployStatusSyncMode.OFF);
        assertFalse(statusSyncService.syncProject(project("moli-server", 204L)));
    }

    @Test
    public void syncProject_uses_ssh_when_server_id_present() {
        OperationProjectDeployInfo project = project("moli-server", 204L);
        OperationDeployStatusVo vo = statusVo(true, true);
        when(operationRemoteDeployService.executeRemoteReadOnly(204L, "user-center", "status", null))
                .thenReturn(vo);

        assertTrue(statusSyncService.syncProject(project));
        assertTrue(project.getDeployRunning());
        verify(operationDeployService, never()).status(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    public void syncProject_uses_local_when_mode_local() {
        when(deployProperties.getStatusSyncMode()).thenReturn(OperationDeployStatusSyncMode.LOCAL);
        OperationProjectDeployInfo project = project("moli-gateway", 204L);
        OperationDeployStatusVo vo = statusVo(true, false);
        when(operationDeployService.status("gateway")).thenReturn(vo);

        assertTrue(statusSyncService.syncProject(project));
        assertFalse(project.getDeployRunning());
        verify(operationRemoteDeployService, never()).executeRemoteReadOnly(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void syncProject_returns_false_when_status_unavailable() {
        OperationProjectDeployInfo project = project("knowledge-server", 204L);
        OperationDeployStatusVo vo = statusVo(false, false);
        vo.setMessage("SSH failed");
        when(operationRemoteDeployService.executeRemoteReadOnly(204L, "knowledge", "status", null))
                .thenReturn(vo);

        assertFalse(statusSyncService.syncProject(project));
    }

    private static OperationProjectDeployInfo project(String name, Long serverId) {
        OperationProjectDeployInfo project = new OperationProjectDeployInfo();
        project.setProjectName(name);
        project.setServerId(serverId);
        return project;
    }

    private static OperationDeployStatusVo statusVo(boolean available, boolean running) {
        OperationDeployStatusVo vo = new OperationDeployStatusVo();
        vo.setAvailable(available);
        vo.setRunning(running);
        return vo;
    }
}
