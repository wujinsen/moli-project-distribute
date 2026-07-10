package com.moli.user.center.server.operation.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.server.operation.config.OperationDeployProperties;
import com.moli.user.center.server.operation.config.OperationUploadProperties;
import com.moli.user.center.server.operation.service.OperationServerService;
import com.moli.user.center.server.operation.service.OperationTaskService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationFileUploadServiceImplTest {

    @InjectMocks
    private OperationFileUploadServiceImpl fileUploadService;

    @Mock
    private OperationServerService operationServerService;
    @Mock
    private OperationTaskService operationTaskService;

    private final OperationUploadProperties uploadProperties = new OperationUploadProperties();
    private final OperationDeployProperties deployProperties = new OperationDeployProperties();

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(fileUploadService, "uploadProperties", uploadProperties);
        ReflectionTestUtils.setField(fileUploadService, "deployProperties", deployProperties);
    }

    @Test(expected = BaseException.class)
    public void createUploadTask_rejects_when_upload_disabled() {
        uploadProperties.setEnabled(false);
        fileUploadService.createUploadTask(sampleFile(), 204L, "/opt/moli-project-distribute/moli-user-center/", "none", null);
    }

    @Test(expected = BaseException.class)
    public void createUploadTask_rejects_empty_file() {
        uploadProperties.setEnabled(true);
        fileUploadService.createUploadTask(new MockMultipartFile("file", new byte[0]), 204L,
                "/opt/moli-project-distribute/moli-user-center/", "none", null);
    }

    @Test(expected = BaseException.class)
    public void createUploadTask_rejects_path_traversal() {
        uploadProperties.setEnabled(true);
        stubServer(204L);
        fileUploadService.createUploadTask(sampleFile(), 204L,
                "/opt/moli-project-distribute/../etc/passwd", "none", null);
    }

    @Test(expected = BaseException.class)
    public void createUploadTask_rejects_path_outside_whitelist() {
        uploadProperties.setEnabled(true);
        stubServer(204L);
        fileUploadService.createUploadTask(sampleFile(), 204L, "/tmp/evil.jar", "none", null);
    }

    @Test(expected = BaseException.class)
    public void createUploadTask_rejects_unzip_on_non_zip() {
        uploadProperties.setEnabled(true);
        stubServer(204L);
        fileUploadService.createUploadTask(sampleFile(), 204L,
                "/opt/moli/frontend/dist/app.jar", "unzipToDist", null);
    }

    @Test(expected = BaseException.class)
    public void createUploadTask_restart_requires_deploy_enabled() {
        uploadProperties.setEnabled(true);
        deployProperties.setEnabled(false);
        stubServer(204L);
        fileUploadService.createUploadTask(sampleFile(), 204L,
                "/opt/moli-project-distribute/moli-user-center/app.jar", "restartService:user-center", null);
    }

    @Test(expected = BaseException.class)
    public void createUploadTask_rejects_unknown_post_action() {
        uploadProperties.setEnabled(true);
        stubServer(204L);
        fileUploadService.createUploadTask(sampleFile(), 204L,
                "/opt/moli-project-distribute/moli-user-center/app.jar", "rm -rf /", null);
    }

    @Test(expected = BaseException.class)
    public void createUploadTask_rejects_unknown_restart_service_key() {
        uploadProperties.setEnabled(true);
        deployProperties.setEnabled(true);
        stubServer(204L);
        fileUploadService.createUploadTask(sampleFile(), 204L,
                "/opt/moli-project-distribute/moli-user-center/app.jar", "restartService:order", null);
    }

    private void stubServer(Long id) {
        OperationServerInfo server = new OperationServerInfo();
        server.setId(id);
        server.setServerName("test-server");
        when(operationServerService.requireEntity(id)).thenReturn(server);
    }

    private static MockMultipartFile sampleFile() {
        return new MockMultipartFile("file", "app.jar", "application/java-archive", "demo".getBytes());
    }
}
