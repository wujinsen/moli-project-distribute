package com.moli.user.center.server.operation.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.entity.OperationTask;
import com.moli.user.center.server.operation.config.OperationDeployProperties;
import com.moli.user.center.server.operation.service.OperationServerService;
import com.moli.user.center.server.operation.service.OperationTaskService;
import com.moli.user.center.server.operation.task.OperationTaskStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationRemoteDeployServiceImplTest {

    @InjectMocks
    private OperationRemoteDeployServiceImpl remoteDeployService;

    @Mock
    private OperationDeployProperties deployProperties;
    @Mock
    private OperationTaskService operationTaskService;
    @Mock
    private OperationServerService operationServerService;

    @Before
    public void enableDeploy() {
        when(deployProperties.isEnabled()).thenReturn(true);
    }

    @Test(expected = BaseException.class)
    public void createDeployTask_rejects_when_deploy_disabled() {
        when(deployProperties.isEnabled()).thenReturn(false);
        remoteDeployService.createDeployTask(null, "user-center", "restart");
    }

    @Test(expected = BaseException.class)
    public void createDeployTask_rejects_read_only_action() {
        remoteDeployService.createDeployTask(null, "user-center", "status");
    }

    @Test(expected = BaseException.class)
    public void createDeployTask_rejects_unknown_service_key() {
        remoteDeployService.createDeployTask(null, "moli-order", "restart");
    }

    @Test
    public void createDeployTask_returns_task_id_for_local_restart() {
        OperationTask task = new OperationTask();
        task.setId(9001L);
        task.setStatus(OperationTaskStatus.PENDING);
        when(operationTaskService.create(eq("deploy"), eq(null), eq("gateway"), eq("restart"), anyString()))
                .thenReturn(task);
        doAnswer(invocation -> null).when(operationTaskService).submit(eq(9001L), eq("deploy:local:gateway"), any());

        Long taskId = remoteDeployService.createDeployTask(null, "gateway", "restart");

        assertEquals(Long.valueOf(9001L), taskId);
    }

    @Test
    public void createDeployTask_returns_task_id_for_remote_server() {
        OperationServerInfo server = new OperationServerInfo();
        server.setId(204L);
        server.setServerName("moli-backend-pro");
        when(operationServerService.requireEntity(204L)).thenReturn(server);

        OperationTask task = new OperationTask();
        task.setId(9002L);
        when(operationTaskService.create(eq("deploy"), eq(204L), eq("knowledge"), eq("stop"), anyString()))
                .thenReturn(task);
        doAnswer(invocation -> null).when(operationTaskService).submit(eq(9002L), eq("deploy:204:knowledge"), any());

        Long taskId = remoteDeployService.createDeployTask(204L, "knowledge", "stop");

        assertEquals(Long.valueOf(9002L), taskId);
    }
}
