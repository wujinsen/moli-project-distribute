package com.moli.user.center.server.api;

import com.moli.common.core.MoliResult;
import com.moli.user.center.common.domain.vo.OperationProjectComponentLinksVo;
import com.moli.user.center.common.domain.vo.OperationRelationEntityVo;
import com.moli.user.center.common.domain.vo.OperationRelationsVo;
import com.moli.user.center.common.domain.vo.OperationTopologyComponentNodeVo;
import com.moli.user.center.common.domain.vo.OperationTopologyGraphVo;
import com.moli.user.center.common.domain.vo.OperationTopologyLinkVo;
import com.moli.user.center.common.domain.vo.OperationTopologyProjectNodeVo;
import com.moli.user.center.common.domain.vo.OperationTopologyServerNodeVo;
import com.moli.user.center.server.operation.controller.OperationProjectController;
import com.moli.user.center.server.operation.controller.OperationRelationsController;
import com.moli.user.center.server.operation.controller.OperationTopologyController;
import com.moli.user.center.server.operation.service.OperationProjectComponentLinkService;
import com.moli.user.center.server.operation.service.OperationRelationService;
import com.moli.user.center.server.operation.service.OperationTopologyService;
import com.moli.user.center.server.testsupport.AbstractApiTest;
import com.moli.user.center.server.testsupport.ControllerTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationRelationsTopologyControllersApiTest extends AbstractApiTest {

    @InjectMocks
    private OperationTopologyController topologyController;
    @InjectMocks
    private OperationRelationsController relationsController;
    @InjectMocks
    private OperationProjectController projectController;

    @Mock
    private OperationTopologyService operationTopologyService;
    @Mock
    private OperationRelationService operationRelationService;
    @Mock
    private OperationProjectComponentLinkService operationProjectComponentLinkService;

    @Test
    public void GET_operation_topology_graph() {
        OperationTopologyGraphVo graph = sampleGraph();
        when(operationTopologyService.getGraph()).thenReturn(graph);

        MoliResult<OperationTopologyGraphVo> result = topologyController.graph();
        ControllerTestSupport.assertSuccess(result);
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getServers().size());
        assertEquals(1, result.getData().getProjects().size());
        assertEquals(1, result.getData().getComponents().size());
        assertEquals(2, result.getData().getLinks().size());
        verify(operationTopologyService).getGraph();
    }

    @Test
    public void GET_operation_relations_project() {
        OperationRelationsVo vo = sampleRelations("project", 401L, "moli-server");
        when(operationRelationService.getRelations("project", 401L)).thenReturn(vo);

        MoliResult<OperationRelationsVo> result = relationsController.relations("project", 401L);
        ControllerTestSupport.assertSuccess(result);
        assertEquals("project", result.getData().getEntityType());
        assertEquals(Long.valueOf(401L), result.getData().getEntity().getId());
        verify(operationRelationService).getRelations("project", 401L);
    }

    @Test
    public void GET_operation_relations_server() {
        OperationRelationsVo vo = sampleRelations("server", 201L, "moli-backend-dev");
        when(operationRelationService.getRelations("server", 201L)).thenReturn(vo);

        MoliResult<OperationRelationsVo> result = relationsController.relations("server", 201L);
        ControllerTestSupport.assertSuccess(result);
        assertEquals("server", result.getData().getEntityType());
        verify(operationRelationService).getRelations("server", 201L);
    }

    @Test
    public void GET_operation_relations_component() {
        OperationRelationsVo vo = sampleRelations("component", 301L, "MySQL");
        when(operationRelationService.getRelations("component", 301L)).thenReturn(vo);

        MoliResult<OperationRelationsVo> result = relationsController.relations("component", 301L);
        ControllerTestSupport.assertSuccess(result);
        assertEquals("component", result.getData().getEntityType());
        verify(operationRelationService).getRelations("component", 301L);
    }

    @Test
    public void GET_operation_project_component_links() {
        OperationProjectComponentLinksVo links = new OperationProjectComponentLinksVo();
        links.setProjectId(401L);
        links.setComponentIds(Arrays.asList(306L, 307L));
        when(operationProjectComponentLinkService.getLinks(401L)).thenReturn(links);

        MoliResult<OperationProjectComponentLinksVo> result = projectController.componentLinks(401L);
        ControllerTestSupport.assertSuccess(result);
        assertEquals(Long.valueOf(401L), result.getData().getProjectId());
        assertEquals(2, result.getData().getComponentIds().size());
        verify(operationProjectComponentLinkService).getLinks(401L);
    }

    @Test
    public void PUT_operation_project_component_links() {
        OperationProjectComponentLinksVo links = new OperationProjectComponentLinksVo();
        links.setComponentIds(Arrays.asList(301L, 302L));
        doNothing().when(operationProjectComponentLinkService).saveLinks(eq(401L), eq(links));

        MoliResult<Boolean> result = projectController.saveComponentLinks(401L, links);
        ControllerTestSupport.assertSuccess(result);
        verify(operationProjectComponentLinkService).saveLinks(401L, links);
    }

    private static OperationTopologyGraphVo sampleGraph() {
        OperationTopologyGraphVo graph = new OperationTopologyGraphVo();

        OperationTopologyServerNodeVo server = new OperationTopologyServerNodeVo();
        server.setId("s-201");
        server.setServerId(201L);
        server.setServerName("moli-backend-dev");
        graph.getServers().add(server);

        OperationTopologyProjectNodeVo project = new OperationTopologyProjectNodeVo();
        project.setId("p-401");
        project.setProjectId(401L);
        project.setProjectName("moli-server");
        project.setDeployRunning(true);
        project.setPortMatchStatus(1);
        graph.getProjects().add(project);

        OperationTopologyComponentNodeVo component = new OperationTopologyComponentNodeVo();
        component.setId("c-307");
        component.setComponentId(307L);
        component.setComponentName("MySQL");
        graph.getComponents().add(component);

        OperationTopologyLinkVo deploy = new OperationTopologyLinkVo();
        deploy.setSource("s-201");
        deploy.setTarget("p-401");
        deploy.setType("deploys");
        graph.getLinks().add(deploy);

        OperationTopologyLinkVo depends = new OperationTopologyLinkVo();
        depends.setSource("p-401");
        depends.setTarget("c-307");
        depends.setType("depends_on");
        graph.getLinks().add(depends);

        return graph;
    }

    private static OperationRelationsVo sampleRelations(String type, Long id, String name) {
        OperationRelationsVo vo = new OperationRelationsVo();
        vo.setEntityType(type);
        OperationRelationEntityVo entity = new OperationRelationEntityVo();
        entity.setEntityType(type);
        entity.setId(id);
        entity.setName(name);
        entity.setEnvironment(1);
        vo.setEntity(entity);
        vo.setServers(Collections.emptyList());
        vo.setProjects(Collections.emptyList());
        vo.setComponents(Collections.emptyList());
        vo.setRecentTasks(Collections.emptyList());
        return vo;
    }
}
