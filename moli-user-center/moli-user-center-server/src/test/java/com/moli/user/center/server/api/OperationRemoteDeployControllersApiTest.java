package com.moli.user.center.server.api;

import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.vo.OperationServerSshVo;
import com.moli.user.center.common.domain.vo.OperationSshTestVo;
import com.moli.user.center.common.domain.vo.OperationTaskVo;
import com.moli.user.center.common.domain.vo.OperationCommandExecVo;
import com.moli.user.center.common.domain.vo.OperationDeployPresetsVo;
import com.moli.user.center.server.operation.controller.OperationCommandController;
import com.moli.user.center.server.operation.controller.OperationFileController;
import com.moli.user.center.server.operation.controller.OperationServerController;
import com.moli.user.center.server.operation.controller.OperationTaskController;
import com.moli.user.center.server.operation.controller.OperationDeployController;
import com.moli.user.center.server.operation.service.OperationCommandService;
import com.moli.user.center.server.operation.service.OperationDeployPresetService;
import com.moli.user.center.server.operation.service.OperationFileUploadService;
import com.moli.user.center.server.operation.service.OperationRemoteDeployService;
import com.moli.user.center.server.operation.service.OperationServerService;
import com.moli.user.center.server.operation.service.OperationTaskService;
import com.moli.user.center.server.testsupport.AbstractApiTest;
import com.moli.user.center.server.testsupport.ControllerTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationRemoteDeployControllersApiTest extends AbstractApiTest {

    @InjectMocks
    private OperationTaskController taskController;
    @InjectMocks
    private OperationFileController fileController;
    @InjectMocks
    private OperationDeployController deployController;
    @InjectMocks
    private OperationServerController serverController;
    @InjectMocks
    private OperationCommandController commandController;

    @Mock
    private OperationTaskService operationTaskService;
    @Mock
    private OperationFileUploadService operationFileUploadService;
    @Mock
    private OperationRemoteDeployService operationRemoteDeployService;
    @Mock
    private OperationDeployPresetService operationDeployPresetService;
    @Mock
    private OperationCommandService operationCommandService;
    @Mock
    private OperationServerService operationServerService;

    @Test
    public void GET_operation_task_poll() {
        when(operationTaskService.poll(1L, 0)).thenReturn(new OperationTaskVo());
        ControllerTestSupport.assertSuccess(taskController.poll(1L, 0));
    }

    @Test
    public void GET_operation_task_list() {
        when(operationTaskService.list(null, null, 1, 10)).thenReturn(new PageRes<>());
        ControllerTestSupport.assertSuccess(taskController.list(null, null, 1, 10));
    }

    @Test
    public void POST_operation_deploy_create_task() {
        when(operationRemoteDeployService.createDeployTask(204L, "user-center", "restart")).thenReturn(42L);
        ControllerTestSupport.assertSuccess(deployController.createTask("user-center", "restart", 204L));
    }

    @Test
    public void GET_operation_deploy_presets() {
        when(operationDeployPresetService.getPresets(204L)).thenReturn(new OperationDeployPresetsVo());
        ControllerTestSupport.assertSuccess(deployController.presets(204L));
    }

    @Test
    public void POST_operation_file_upload() {
        MockMultipartFile file = new MockMultipartFile("file", "app.jar", "application/java-archive", "x".getBytes());
        when(operationFileUploadService.createUploadTask(any(), eq(204L), any(), eq("none"), eq(null))).thenReturn(99L);
        ControllerTestSupport.assertSuccess(
                fileController.upload(file, 204L, "/opt/moli-project-distribute/moli-user-center/app.jar", "none", null));
    }

    @Test
    public void POST_operation_command_exec_task() {
        OperationCommandExecVo body = new OperationCommandExecVo();
        body.setServerId(204L);
        body.setCommand("ls -la");
        when(operationCommandService.createCommandTask(204L, "ls -la", null)).thenReturn(77L);
        ControllerTestSupport.assertSuccess(commandController.createTask(body));
    }

    @Test
    public void PUT_operation_server_ssh() {
        doNothing().when(operationServerService).saveSsh(eq(204L), any());
        ControllerTestSupport.assertSuccess(serverController.saveSsh(204L, new OperationServerSshVo()));
    }

    @Test
    public void POST_operation_server_ssh_test() {
        when(operationServerService.testSsh(204L)).thenReturn(new OperationSshTestVo());
        ControllerTestSupport.assertSuccess(serverController.testSsh(204L));
    }
}
