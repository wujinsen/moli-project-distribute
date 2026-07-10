package com.moli.user.center.server.api;

import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationPlatformInfo;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.vo.OperationComponentVo;
import com.moli.user.center.common.domain.vo.OperationPlatformVo;
import com.moli.user.center.common.domain.vo.OperationSecretRevealVo;
import com.moli.user.center.common.domain.vo.OperationServerInfoVo;
import com.moli.user.center.common.domain.vo.OperationProjectVo;
import com.moli.user.center.common.domain.vo.OperationServerLinksVo;
import com.moli.user.center.common.domain.vo.OperationServerTopologyVo;
import com.moli.user.center.common.domain.vo.OperationServerVo;
import com.moli.user.center.common.domain.vo.OperationDeployStatusVo;
import com.moli.user.center.common.domain.vo.OperationPortAuditVo;
import com.moli.user.center.common.domain.vo.OperationStatsVo;
import com.moli.user.center.server.operation.controller.OperationAuditController;
import com.moli.user.center.server.operation.controller.OperationComponentController;
import com.moli.user.center.server.operation.controller.OperationDeployController;
import com.moli.user.center.server.operation.controller.OperationHealthController;
import com.moli.user.center.server.operation.controller.OperationPlatformController;
import com.moli.user.center.server.operation.controller.OperationProjectController;
import com.moli.user.center.server.operation.controller.OperationServerController;
import com.moli.user.center.server.operation.controller.OperationStatsController;
import com.moli.user.center.common.domain.vo.OperationHealthProbeResultVo;
import com.moli.user.center.server.operation.service.OperationHealthProbeService;
import com.moli.user.center.server.operation.service.OperationProjectService;
import com.moli.user.center.server.operation.service.OperationServerLinkService;
import com.moli.user.center.server.operation.service.OperationAuditService;
import com.moli.user.center.server.operation.service.OperationComponentService;
import com.moli.user.center.server.operation.service.OperationDeployService;
import com.moli.user.center.server.operation.service.OperationPlatformService;
import com.moli.user.center.server.operation.service.OperationServerService;
import com.moli.user.center.server.operation.service.OperationStatsService;
import com.moli.user.center.server.testsupport.AbstractApiTest;
import com.moli.user.center.server.testsupport.ControllerTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationControllersApiTest extends AbstractApiTest {

    @InjectMocks
    private OperationPlatformController platformController;
    @InjectMocks
    private OperationServerController serverController;
    @InjectMocks
    private OperationProjectController projectController;
    @InjectMocks
    private OperationComponentController componentController;
    @InjectMocks
    private OperationAuditController auditController;
    @InjectMocks
    private OperationStatsController statsController;
    @InjectMocks
    private OperationDeployController deployController;
    @InjectMocks
    private OperationHealthController healthController;

    @Mock
    private OperationServerService operationServerService;
    @Mock
    private OperationServerLinkService operationServerLinkService;
    @Mock
    private OperationPlatformService operationPlatformService;
    @Mock
    private OperationComponentService operationComponentService;
    @Mock
    private OperationAuditService operationAuditService;
    @Mock
    private OperationStatsService operationStatsService;
    @Mock
    private OperationDeployService operationDeployService;
    @Mock
    private OperationProjectService operationProjectService;
    @Mock
    private OperationHealthProbeService operationHealthProbeService;

    @Test
    public void GET_operation_platform_list() {
        when(operationPlatformService.list(any())).thenReturn(new PageRes<>());
        OperationPlatformInfo q = new OperationPlatformInfo();
        q.setPageNum(1);
        q.setPageSize(10);
        ControllerTestSupport.assertSuccess(platformController.list(q));
    }

    @Test
    public void POST_operation_platform_insert() {
        doNothing().when(operationPlatformService).create(any());
        ControllerTestSupport.assertSuccess(platformController.insert(new OperationPlatformInfo()));
    }

    @Test
    public void PUT_operation_platform_update() {
        doNothing().when(operationPlatformService).update(any());
        ControllerTestSupport.assertSuccess(platformController.update(new OperationPlatformInfo()));
    }

    @Test
    public void GET_operation_platform_id() {
        when(operationPlatformService.getById(1L)).thenReturn(new OperationPlatformVo());
        ControllerTestSupport.assertSuccess(platformController.selectOne(1L));
    }

    @Test
    public void GET_operation_platform_secret() {
        when(operationPlatformService.revealPassword(1L)).thenReturn(new OperationSecretRevealVo("secret"));
        ControllerTestSupport.assertSuccess(platformController.revealSecret(1L));
    }

    @Test
    public void DELETE_operation_platform_ids() {
        doNothing().when(operationPlatformService).deleteByIds(any());
        ControllerTestSupport.assertSuccess(platformController.remove(new Long[]{1L}));
    }

    @Test
    public void GET_operation_server_list() {
        when(operationServerService.list(any())).thenReturn(new PageRes<>());
        OperationServerInfoVo q = new OperationServerInfoVo();
        q.setPageNum(1);
        q.setPageSize(10);
        ControllerTestSupport.assertSuccess(serverController.list(q));
    }

    @Test
    public void POST_operation_server_insert() {
        doNothing().when(operationServerService).create(any());
        ControllerTestSupport.assertSuccess(serverController.insert(new OperationServerInfo()));
    }

    @Test
    public void PUT_operation_server_update() {
        doNothing().when(operationServerService).update(any());
        ControllerTestSupport.assertSuccess(serverController.update(new OperationServerInfo()));
    }

    @Test
    public void GET_operation_server_id() {
        when(operationServerService.getById(1L)).thenReturn(new OperationServerVo());
        ControllerTestSupport.assertSuccess(serverController.selectOne(1L));
    }

    @Test
    public void GET_operation_server_topology() {
        when(operationServerService.getTopology(1L)).thenReturn(new OperationServerTopologyVo());
        ControllerTestSupport.assertSuccess(serverController.topology(1L));
    }

    @Test
    public void POST_operation_server_check() {
        when(operationServerService.checkHealth(1L)).thenReturn(new OperationServerVo());
        ControllerTestSupport.assertSuccess(serverController.checkHealth(1L));
    }

    @Test
    public void GET_operation_server_links() {
        when(operationServerLinkService.getLinks(1L)).thenReturn(new OperationServerLinksVo());
        ControllerTestSupport.assertSuccess(serverController.links(1L));
    }

    @Test
    public void PUT_operation_server_links() {
        doNothing().when(operationServerLinkService).saveLinks(any(), any());
        ControllerTestSupport.assertSuccess(serverController.saveLinks(1L, new OperationServerLinksVo()));
    }

    @Test
    public void DELETE_operation_server_ids() {
        doNothing().when(operationServerService).deleteByIds(any());
        ControllerTestSupport.assertSuccess(serverController.remove(new Long[]{1L}));
    }

    @Test
    public void GET_operation_project_list() {
        when(operationProjectService.list(any())).thenReturn(new PageRes<>());
        OperationProjectDeployInfo q = new OperationProjectDeployInfo();
        q.setPageNum(1);
        q.setPageSize(10);
        ControllerTestSupport.assertSuccess(projectController.list(q));
    }

    @Test
    public void POST_operation_project_insert() {
        doNothing().when(operationProjectService).create(any());
        ControllerTestSupport.assertSuccess(projectController.insert(new OperationProjectDeployInfo()));
    }

    @Test
    public void PUT_operation_project_update() {
        doNothing().when(operationProjectService).update(any());
        ControllerTestSupport.assertSuccess(projectController.update(new OperationProjectDeployInfo()));
    }

    @Test
    public void GET_operation_project_id() {
        when(operationProjectService.getById(1L)).thenReturn(new OperationProjectVo());
        ControllerTestSupport.assertSuccess(projectController.selectOne(1L));
    }

    @Test
    public void DELETE_operation_project_ids() {
        ControllerTestSupport.assertSuccess(projectController.remove(new Long[]{1L}));
    }

    @Test
    public void GET_operation_component_list() {
        when(operationComponentService.list(any())).thenReturn(new PageRes<>());
        OperationComponentDeployInfo q = new OperationComponentDeployInfo();
        q.setPageNum(1);
        q.setPageSize(10);
        ControllerTestSupport.assertSuccess(componentController.list(q));
    }

    @Test
    public void POST_operation_component_insert() {
        doNothing().when(operationComponentService).create(any());
        ControllerTestSupport.assertSuccess(componentController.insert(new OperationComponentDeployInfo()));
    }

    @Test
    public void PUT_operation_component_update() {
        doNothing().when(operationComponentService).update(any());
        ControllerTestSupport.assertSuccess(componentController.update(new OperationComponentDeployInfo()));
    }

    @Test
    public void GET_operation_component_id() {
        when(operationComponentService.getById(1L)).thenReturn(new OperationComponentVo());
        ControllerTestSupport.assertSuccess(componentController.selectOne(1L));
    }

    @Test
    public void GET_operation_component_secret() {
        when(operationComponentService.revealPassword(1L)).thenReturn(new OperationSecretRevealVo("secret"));
        ControllerTestSupport.assertSuccess(componentController.revealSecret(1L));
    }

    @Test
    public void POST_operation_component_check() {
        when(operationComponentService.checkHealth(1L)).thenReturn(new OperationComponentVo());
        ControllerTestSupport.assertSuccess(componentController.checkHealth(1L));
    }

    @Test
    public void DELETE_operation_component_ids() {
        doNothing().when(operationComponentService).deleteByIds(any());
        ControllerTestSupport.assertSuccess(componentController.remove(new Long[]{1L}));
    }

    @Test
    public void GET_operation_audit_port_matrix() {
        when(operationAuditService.auditPortMatrix()).thenReturn(new OperationPortAuditVo());
        ControllerTestSupport.assertSuccess(auditController.portMatrix());
    }

    @Test
    public void GET_operation_stats() {
        when(operationStatsService.getStats()).thenReturn(new OperationStatsVo());
        ControllerTestSupport.assertSuccess(statsController.stats());
    }

    @Test
    public void GET_operation_deploy_status() {
        when(operationDeployService.status("user-center")).thenReturn(new OperationDeployStatusVo());
        ControllerTestSupport.assertSuccess(deployController.status("user-center", null));
    }

    @Test
    public void POST_operation_deploy_restart() {
        OperationDeployStatusVo vo = new OperationDeployStatusVo();
        vo.setRunning(true);
        when(operationDeployService.execute("user-center", "restart", null)).thenReturn(vo);
        ControllerTestSupport.assertSuccess(deployController.execute("user-center", "restart", null, null));
    }

    @Test
    public void POST_operation_health_probe_all() {
        when(operationHealthProbeService.probeAll()).thenReturn(new OperationHealthProbeResultVo());
        ControllerTestSupport.assertSuccess(healthController.probeAll());
    }
}
