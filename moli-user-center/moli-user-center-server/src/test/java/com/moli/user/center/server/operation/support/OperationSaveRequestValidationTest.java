package com.moli.user.center.server.operation.support;

import com.moli.user.center.common.domain.dto.operation.OperationCommandExecRequest;
import com.moli.user.center.common.domain.dto.operation.OperationComponentSaveRequest;
import com.moli.user.center.common.domain.dto.operation.OperationDeployTaskRequest;
import com.moli.user.center.common.domain.dto.operation.OperationFileUploadRequest;
import com.moli.user.center.common.domain.dto.operation.OperationPortMatrixSaveRequest;
import com.moli.user.center.common.domain.dto.operation.OperationPlatformSaveRequest;
import com.moli.user.center.common.domain.dto.operation.OperationProjectSaveRequest;
import com.moli.user.center.common.domain.dto.operation.OperationServerSaveRequest;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OperationSaveRequestValidationTest {

    private Validator validator;

    @Before
    public void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    public void platform_requires_name() {
        OperationPlatformSaveRequest req = new OperationPlatformSaveRequest();
        Set<ConstraintViolation<OperationPlatformSaveRequest>> violations = validator.validate(req);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("platformName")));
    }

    @Test
    public void server_requires_ip_and_name() {
        OperationServerSaveRequest req = new OperationServerSaveRequest();
        Set<ConstraintViolation<OperationServerSaveRequest>> violations = validator.validate(req);
        assertTrue(violations.size() >= 2);
    }

    @Test
    public void project_requires_name_and_server_ref() {
        OperationProjectSaveRequest req = new OperationProjectSaveRequest();
        req.setProjectName("moli-server");
        Set<ConstraintViolation<OperationProjectSaveRequest>> violations = validator.validate(req);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("serverId")));
    }

    @Test
    public void project_accepts_serverIp_only() {
        OperationProjectSaveRequest req = new OperationProjectSaveRequest();
        req.setProjectName("moli-server");
        req.setServerIp("127.0.0.1");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    public void project_rejects_invalid_port() {
        OperationProjectSaveRequest req = new OperationProjectSaveRequest();
        req.setProjectName("moli-server");
        req.setServerId(201L);
        req.setPort("abc");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    public void component_accepts_serverId() {
        OperationComponentSaveRequest req = new OperationComponentSaveRequest();
        req.setComponentName("Redis");
        req.setServerId(201L);
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    public void deployTask_requires_valid_service_key_and_action() {
        OperationDeployTaskRequest req = new OperationDeployTaskRequest();
        req.setAction("restart");
        assertFalse(validator.validate(req).isEmpty());

        req.setServiceKey("moli-order");
        assertFalse(validator.validate(req).isEmpty());

        req.setServiceKey("user-center");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    public void fileUpload_requires_server_and_path() {
        OperationFileUploadRequest req = new OperationFileUploadRequest();
        assertFalse(validator.validate(req).isEmpty());

        req.setServerId(204L);
        req.setTargetPath("/opt/moli-project-distribute/moli-user-center/app.jar");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    public void fileUpload_rejects_invalid_post_action() {
        OperationFileUploadRequest req = new OperationFileUploadRequest();
        req.setServerId(204L);
        req.setTargetPath("/opt/moli-project-distribute/moli-user-center/app.jar");
        req.setPostAction("evil");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    public void portMatrix_requires_matrix_key_and_port() {
        OperationPortMatrixSaveRequest req = new OperationPortMatrixSaveRequest();
        assertFalse(validator.validate(req).isEmpty());

        req.setMatrixKey("User-Center");
        req.setExpectedPort("8888");
        assertFalse(validator.validate(req).isEmpty());

        req.setMatrixKey("user-center");
        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    public void portMatrix_rejects_invalid_matrix_key_pattern() {
        OperationPortMatrixSaveRequest req = new OperationPortMatrixSaveRequest();
        req.setMatrixKey("1bad");
        req.setExpectedPort("9000");
        assertFalse(validator.validate(req).isEmpty());
    }
}
