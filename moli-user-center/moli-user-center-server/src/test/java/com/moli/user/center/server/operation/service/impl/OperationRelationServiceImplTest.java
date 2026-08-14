package com.moli.user.center.server.operation.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.entity.OperationTask;
import com.moli.user.center.common.domain.vo.OperationRelationsVo;
import com.moli.user.center.server.operation.audit.OperationPortMatrixPortCheck;
import com.moli.user.center.server.operation.audit.OperationPortMatrixProvider;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.mapper.OperationTaskMapper;
import com.moli.user.center.server.operation.support.OperationRelationQuerySupport;
import com.moli.user.center.server.testsupport.MybatisPlusTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationRelationServiceImplTest {

    @InjectMocks
    private OperationRelationServiceImpl relationService;

    @Mock
    private OperationRelationQuerySupport relationQuerySupport;
    @Mock
    private OperationServerMapper operationServerMapper;
    @Mock
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Mock
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;
    @Mock
    private OperationPortMatrixProvider portMatrixProvider;
    @Mock
    private OperationTaskMapper operationTaskMapper;

    @Before
    public void setUp() {
        MybatisPlusTestSupport.initAll();
    }

    @Test
    public void getRelations_forProject_returns_servers_components_and_tasks() {
        OperationProjectDeployInfo project = project(401L, "moli-user-center", 201L);
        when(operationProjectDeployInfoMapper.selectById(401L)).thenReturn(project);
        when(relationQuerySupport.resolveServerIdsForProject(401L, 201L)).thenReturn(Arrays.asList(201L, 202L));
        when(relationQuerySupport.resolveComponentIdsForProject(401L)).thenReturn(Collections.singletonList(301L));

        when(operationServerMapper.selectBatchIds(any()))
                .thenReturn(Arrays.asList(server(201L, "app-1"), server(202L, "app-2")));
        when(operationComponentDeployInfoMapper.selectBatchIds(any()))
                .thenReturn(Collections.singletonList(component(301L, "MySQL")));
        when(portMatrixProvider.check(any(), any())).thenReturn(matchedCheck());

        OperationTask task = new OperationTask();
        task.setId(9001L);
        task.setTaskType("deploy");
        task.setAction("restart");
        task.setStatus("success");
        task.setCreateTime(new Date());
        when(operationTaskMapper.selectList(any())).thenReturn(Collections.singletonList(task));

        OperationRelationsVo vo = relationService.getRelations("project", 401L);

        assertEquals("project", vo.getEntityType());
        assertEquals(Long.valueOf(401L), vo.getEntity().getId());
        assertEquals("moli-user-center", vo.getEntity().getName());
        assertEquals(2, vo.getServers().size());
        assertTrue(vo.getServers().stream().anyMatch(s -> Boolean.TRUE.equals(s.getPrimary())));
        assertEquals(1, vo.getComponents().size());
        assertEquals(1, vo.getRecentTasks().size());
        assertEquals(Long.valueOf(9001L), vo.getRecentTasks().get(0).getId());
    }

    @Test
    public void getRelations_forServer_returns_projects_and_components() {
        when(operationServerMapper.selectById(201L)).thenReturn(server(201L, "app-1"));
        when(relationQuerySupport.resolveProjectIdsForServer(201L)).thenReturn(Collections.singletonList(401L));
        when(relationQuerySupport.resolveComponentIdsForServer(201L)).thenReturn(Collections.singletonList(301L));

        when(operationProjectDeployInfoMapper.selectBatchIds(any()))
                .thenReturn(Collections.singletonList(project(401L, "moli-user-center", 201L)));
        when(operationComponentDeployInfoMapper.selectBatchIds(any()))
                .thenReturn(Collections.singletonList(component(301L, "MySQL")));
        when(portMatrixProvider.check(any(), any())).thenReturn(matchedCheck());
        when(operationTaskMapper.selectList(any())).thenReturn(Collections.emptyList());

        OperationRelationsVo vo = relationService.getRelations("SERVER", 201L);

        assertEquals("server", vo.getEntityType());
        assertEquals(1, vo.getProjects().size());
        assertEquals(1, vo.getComponents().size());
        assertTrue(vo.getServers().isEmpty());
    }

    @Test
    public void getRelations_forComponent_returns_servers_and_projects() {
        OperationComponentDeployInfo component = component(301L, "MySQL");
        component.setServerId(201L);
        when(operationComponentDeployInfoMapper.selectById(301L)).thenReturn(component);
        when(relationQuerySupport.resolveServerIdsForComponent(301L, 201L)).thenReturn(Collections.singletonList(201L));
        when(relationQuerySupport.resolveProjectIdsForComponent(301L)).thenReturn(Collections.singletonList(401L));

        when(operationServerMapper.selectBatchIds(any()))
                .thenReturn(Collections.singletonList(server(201L, "app-1")));
        when(operationProjectDeployInfoMapper.selectBatchIds(any()))
                .thenReturn(Collections.singletonList(project(401L, "moli-user-center", 201L)));
        when(portMatrixProvider.check(any(), any())).thenReturn(matchedCheck());
        when(operationTaskMapper.selectList(any())).thenReturn(Collections.emptyList());

        OperationRelationsVo vo = relationService.getRelations("component", 301L);

        assertEquals("component", vo.getEntityType());
        assertEquals(1, vo.getServers().size());
        assertTrue(vo.getServers().get(0).getPrimary());
        assertEquals(1, vo.getProjects().size());
        assertTrue(vo.getComponents().isEmpty());
    }

    @Test
    public void getRelations_rejects_unknown_entity_type() {
        try {
            relationService.getRelations("platform", 1L);
            fail("expected invalid entity type");
        } catch (BaseException ex) {
            assertTrue(ex.getMessage().contains("不支持的实体类型"));
        }
    }

    @Test
    public void getRelations_rejects_missing_project() {
        when(operationProjectDeployInfoMapper.selectById(999L)).thenReturn(null);
        try {
            relationService.getRelations("project", 999L);
            fail("expected missing project");
        } catch (BaseException ignored) {
        }
    }

    private static OperationPortMatrixPortCheck matchedCheck() {
        return new OperationPortMatrixPortCheck(1, "8080", "user-center", "ok");
    }

    private static OperationServerInfo server(Long id, String name) {
        OperationServerInfo row = new OperationServerInfo();
        row.setId(id);
        row.setServerName(name);
        row.setIp("10.0.0." + id);
        row.setEnvironment(1);
        row.setServerRole("app");
        row.setStatus(1);
        return row;
    }

    private static OperationProjectDeployInfo project(Long id, String name, Long serverId) {
        OperationProjectDeployInfo row = new OperationProjectDeployInfo();
        row.setId(id);
        row.setProjectName(name);
        row.setServerId(serverId);
        row.setPort("8080");
        row.setEnvironment(1);
        row.setDeployRunning(Boolean.TRUE);
        return row;
    }

    private static OperationComponentDeployInfo component(Long id, String name) {
        OperationComponentDeployInfo row = new OperationComponentDeployInfo();
        row.setId(id);
        row.setComponentName(name);
        row.setPort("3306");
        row.setEnvironment(1);
        row.setStatus(1);
        return row;
    }
}
