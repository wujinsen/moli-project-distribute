package com.moli.user.center.server.operation.support;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.dto.operation.OperationDeployTaskRequest;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.server.operation.deploy.OperationDeployServiceRegistry;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationDeployTaskProjectSupportTest {

    @InjectMocks
    private OperationDeployTaskProjectSupport projectSupport;

    @Mock
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Mock
    private OperationDeployServiceRegistry deployServiceRegistry;

    @Before
    public void stubRegistry() {
        when(deployServiceRegistry.requireKnownKey("user-center")).thenReturn("user-center");
        when(deployServiceRegistry.resolveProjectName("moli-server")).thenReturn("user-center");
    }

    @Test
    public void resolve_skips_when_no_project_id() {
        OperationDeployTaskRequest req = req(null, 204L, "user-center", "restart");
        OperationDeployTaskProjectSupport.DeployTaskBinding binding = projectSupport.resolve(req);
        assertEquals(Long.valueOf(204L), binding.getServerId());
        assertNull(binding.getProjectId());
    }

    @Test
    public void resolve_fills_server_id_from_project() {
        OperationProjectDeployInfo project = new OperationProjectDeployInfo();
        project.setId(401L);
        project.setProjectName("moli-server");
        project.setServerId(204L);
        when(operationProjectDeployInfoMapper.selectById(401L)).thenReturn(project);

        OperationDeployTaskRequest req = req(401L, null, "user-center", "restart");
        OperationDeployTaskProjectSupport.DeployTaskBinding binding = projectSupport.resolve(req);

        assertEquals(Long.valueOf(204L), binding.getServerId());
        assertEquals(Long.valueOf(401L), binding.getProjectId());
    }

    @Test(expected = BaseException.class)
    public void resolve_rejects_mismatched_service_key() {
        OperationProjectDeployInfo project = new OperationProjectDeployInfo();
        project.setId(401L);
        project.setProjectName("moli-server");
        project.setServerId(204L);
        when(operationProjectDeployInfoMapper.selectById(401L)).thenReturn(project);
        when(deployServiceRegistry.requireKnownKey("gateway")).thenReturn("gateway");

        projectSupport.resolve(req(401L, 204L, "gateway", "restart"));
    }

    @Test(expected = BaseException.class)
    public void resolve_rejects_conflicting_server_id() {
        OperationProjectDeployInfo project = new OperationProjectDeployInfo();
        project.setId(401L);
        project.setProjectName("moli-server");
        project.setServerId(204L);
        when(operationProjectDeployInfoMapper.selectById(401L)).thenReturn(project);

        projectSupport.resolve(req(401L, 205L, "user-center", "restart"));
    }

    private static OperationDeployTaskRequest req(Long projectId, Long serverId,
                                                  String serviceKey, String action) {
        OperationDeployTaskRequest req = new OperationDeployTaskRequest();
        req.setProjectId(projectId);
        req.setServerId(serverId);
        req.setServiceKey(serviceKey);
        req.setAction(action);
        return req;
    }
}
