package com.moli.user.center.server.operation.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.dto.operation.OperationDeployTaskRequest;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.entity.OperationTask;
import com.moli.user.center.common.domain.vo.OperationDeployStatusVo;
import com.moli.user.center.server.operation.config.OperationDeployProperties;
import com.moli.user.center.server.operation.deploy.OperationDeployServiceRegistry;
import com.moli.user.center.server.operation.service.OperationServerService;
import com.moli.user.center.server.operation.service.OperationTaskService;
import com.moli.user.center.server.operation.ssh.OperationSshClient;
import com.moli.user.center.server.operation.ssh.OperationSshCommandResult;
import com.moli.user.center.server.operation.ssh.OperationSshSession;
import com.moli.user.center.server.operation.support.OperationBizException;
import com.moli.user.center.server.operation.support.OperationDeployLocalPolicy;
import com.moli.user.center.server.operation.support.OperationDeployTaskProjectSupport;
import com.moli.user.center.server.operation.task.OperationTaskStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
    @Mock
    private OperationSshClient sshClient;
    @Mock
    private OperationDeployServiceRegistry deployServiceRegistry;

    private OperationDeployLocalPolicy deployLocalPolicy;
    private OperationDeployTaskProjectSupport deployTaskProjectSupport;

    @Before
    public void enableDeploy() {
        deployLocalPolicy = new OperationDeployLocalPolicy();
        deployTaskProjectSupport = new OperationDeployTaskProjectSupport();
        ReflectionTestUtils.setField(deployLocalPolicy, "deployProperties", deployProperties);
        ReflectionTestUtils.setField(deployTaskProjectSupport, "operationProjectDeployInfoMapper",
                org.mockito.Mockito.mock(com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper.class));
        ReflectionTestUtils.setField(deployTaskProjectSupport, "deployServiceRegistry", deployServiceRegistry);
        ReflectionTestUtils.setField(remoteDeployService, "deployLocalPolicy", deployLocalPolicy);
        ReflectionTestUtils.setField(remoteDeployService, "deployTaskProjectSupport", deployTaskProjectSupport);
        when(deployProperties.isEnabled()).thenReturn(true);
        when(deployProperties.isAllowLocal()).thenReturn(true);
        when(deployProperties.getDeployRoot()).thenReturn("/opt/moli-project-distribute");
        when(deployServiceRegistry.requireKnownKey(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> {
                    String key = invocation.getArgument(0);
                    if ("moli-order".equals(key)) {
                        throw OperationBizException.params("不支持的 serviceKey: " + key);
                    }
                    return key;
                });
    }

    @Test(expected = BaseException.class)
    public void createDeployTask_rejects_when_deploy_disabled() {
        when(deployProperties.isEnabled()).thenReturn(false);
        remoteDeployService.createDeployTask(taskRequest(null, "user-center", "restart"));
    }

    @Test
    public void createDeployTask_rejects_when_deploy_disabled_with_code() {
        when(deployProperties.isEnabled()).thenReturn(false);
        try {
            remoteDeployService.createDeployTask(taskRequest(null, "user-center", "restart"));
        } catch (BaseException ex) {
            assertEquals(Integer.valueOf(OperationBizException.CODE_DEPLOY_DISABLED), ex.getErrorCode());
            return;
        }
        throw new AssertionError("expected deploy disabled");
    }

    @Test(expected = BaseException.class)
    public void createDeployTask_rejects_read_only_action() {
        remoteDeployService.createDeployTask(taskRequest(null, "user-center", "status"));
    }

    @Test(expected = BaseException.class)
    public void createDeployTask_rejects_unknown_service_key() {
        remoteDeployService.createDeployTask(taskRequest(null, "moli-order", "restart"));
    }

    @Test
    public void createDeployTask_rejects_local_when_allow_local_disabled() {
        when(deployProperties.isAllowLocal()).thenReturn(false);
        try {
            remoteDeployService.createDeployTask(taskRequest(null, "user-center", "restart"));
        } catch (BaseException ex) {
            assertEquals(Integer.valueOf(OperationBizException.CODE_LOCAL_DEPLOY_DISABLED), ex.getErrorCode());
            return;
        }
        throw new AssertionError("expected local deploy disabled");
    }

    @Test
    public void createDeployTask_allows_remote_when_allow_local_disabled() {
        when(deployProperties.isAllowLocal()).thenReturn(false);
        OperationServerInfo server = new OperationServerInfo();
        server.setId(204L);
        server.setServerName("moli-backend-pro");
        when(operationServerService.requireEntity(204L)).thenReturn(server);

        OperationTask task = new OperationTask();
        task.setId(9003L);
        when(operationTaskService.create(eq("deploy"), eq(204L), eq(null), eq("user-center"), eq("restart"), anyString()))
                .thenReturn(task);
        doAnswer(invocation -> null).when(operationTaskService).submit(eq(9003L), eq("deploy:204:user-center"), any());

        Long taskId = remoteDeployService.createDeployTask(taskRequest(204L, "user-center", "restart"));

        assertEquals(Long.valueOf(9003L), taskId);
    }

    @Test
    public void createDeployTask_returns_task_id_for_local_restart() {
        OperationTask task = new OperationTask();
        task.setId(9001L);
        task.setStatus(OperationTaskStatus.PENDING);
        when(operationTaskService.create(eq("deploy"), eq(null), eq(null), eq("gateway"), eq("restart"), anyString()))
                .thenReturn(task);
        doAnswer(invocation -> null).when(operationTaskService).submit(eq(9001L), eq("deploy:local:gateway"), any());

        Long taskId = remoteDeployService.createDeployTask(taskRequest(null, "gateway", "restart"));

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
        when(operationTaskService.create(eq("deploy"), eq(204L), eq(null), eq("knowledge"), eq("stop"), anyString()))
                .thenReturn(task);
        doAnswer(invocation -> null).when(operationTaskService).submit(eq(9002L), eq("deploy:204:knowledge"), any());

        Long taskId = remoteDeployService.createDeployTask(taskRequest(204L, "knowledge", "stop"));

        assertEquals(Long.valueOf(9002L), taskId);
    }

    @Test
    public void executeRemoteReadOnly_uploads_script_when_missing() throws Exception {
        OperationServerInfo server = new OperationServerInfo();
        server.setId(204L);
        server.setServerName("moli-backend-pro");
        when(operationServerService.requireEntity(204L)).thenReturn(server);

        Path tempScript = Files.createTempFile("moli-service", ".sh");
        Files.write(tempScript, "#!/bin/bash\necho is running".getBytes());
        when(deployProperties.getScriptPath()).thenReturn(tempScript.toString());

        OperationSshSession session = mock(OperationSshSession.class);
        when(sshClient.connect(server)).thenReturn(session);
        when(sshClient.sftpExists(eq(session), anyString())).thenReturn(false);
        when(sshClient.exec(eq(session), anyString(), eq(null)))
                .thenReturn(new OperationSshCommandResult(0, "is running"));

        OperationDeployStatusVo vo = remoteDeployService.executeRemoteReadOnly(204L, "gateway", "status", null);

        verify(sshClient).sftpPutText(eq(session), anyString(), anyString(), eq(true));
        assertTrue(vo.getAvailable());
        assertTrue(vo.getRunning());
        Files.deleteIfExists(tempScript);
    }

    private static OperationDeployTaskRequest taskRequest(Long serverId, String serviceKey, String action) {
        OperationDeployTaskRequest req = new OperationDeployTaskRequest();
        req.setServerId(serverId);
        req.setServiceKey(serviceKey);
        req.setAction(action);
        return req;
    }
}
