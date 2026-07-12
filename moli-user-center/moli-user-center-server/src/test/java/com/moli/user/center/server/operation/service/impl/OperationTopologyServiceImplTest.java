package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectComponent;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerComponent;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.entity.OperationServerProject;
import com.moli.user.center.common.domain.vo.OperationTopologyGraphVo;
import com.moli.user.center.common.domain.vo.OperationTopologyLinkVo;
import com.moli.user.center.server.operation.audit.OperationPortMatrixPortCheck;
import com.moli.user.center.server.operation.audit.OperationPortMatrixProvider;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectComponentLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerComponentLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.mapper.OperationServerProjectLinkMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationTopologyServiceImplTest {

    @InjectMocks
    private OperationTopologyServiceImpl topologyService;

    @Mock
    private OperationServerMapper operationServerMapper;
    @Mock
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Mock
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;
    @Mock
    private OperationServerProjectLinkMapper operationServerProjectLinkMapper;
    @Mock
    private OperationServerComponentLinkMapper operationServerComponentLinkMapper;
    @Mock
    private OperationProjectComponentLinkMapper operationProjectComponentLinkMapper;
    @Mock
    private OperationPortMatrixProvider portMatrixProvider;

    @Test
    public void getGraph_builds_prefixed_nodes_and_deploys_edges() {
        OperationServerInfo server = server(201L, "app-1");
        OperationProjectDeployInfo project = project(401L, "moli-user-center", 201L);
        OperationComponentDeployInfo component = component(301L, "MySQL", 201L);

        when(operationServerMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(server));
        when(operationProjectDeployInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(project));
        when(operationComponentDeployInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(component));

        OperationServerProject sp = new OperationServerProject();
        sp.setServerId(201L);
        sp.setProjectId(401L);
        when(operationServerProjectLinkMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(sp));

        OperationServerComponent sc = new OperationServerComponent();
        sc.setServerId(201L);
        sc.setComponentId(301L);
        when(operationServerComponentLinkMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(sc));

        when(operationProjectComponentLinkMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());
        when(portMatrixProvider.check(any(), any())).thenReturn(new OperationPortMatrixPortCheck(1, "8080", "user-center", "ok"));

        OperationTopologyGraphVo graph = topologyService.getGraph();

        assertEquals(1, graph.getServers().size());
        assertEquals("s-201", graph.getServers().get(0).getId());
        assertEquals(1, graph.getProjects().size());
        assertEquals("p-401", graph.getProjects().get(0).getId());
        assertEquals(1, graph.getComponents().size());
        assertEquals("c-301", graph.getComponents().get(0).getId());

        List<String> deploySources = graph.getLinks().stream()
                .filter(l -> "deploys".equals(l.getType()))
                .map(OperationTopologyLinkVo::getSource)
                .collect(Collectors.toList());
        assertTrue(deploySources.contains("s-201"));
        assertEquals(2, graph.getLinks().stream().filter(l -> "deploys".equals(l.getType())).count());
    }

    @Test
    public void getGraph_dedupes_duplicate_deploy_edges_from_nn_and_primary() {
        OperationServerInfo server = server(201L, "app-1");
        OperationProjectDeployInfo project = project(401L, "moli-user-center", 201L);

        when(operationServerMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(server));
        when(operationProjectDeployInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(project));
        when(operationComponentDeployInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        OperationServerProject sp = new OperationServerProject();
        sp.setServerId(201L);
        sp.setProjectId(401L);
        when(operationServerProjectLinkMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(sp));
        when(operationServerComponentLinkMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());
        when(operationProjectComponentLinkMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());
        when(portMatrixProvider.check(any(), any())).thenReturn(new OperationPortMatrixPortCheck(1, "8080", "user-center", "ok"));

        OperationTopologyGraphVo graph = topologyService.getGraph();

        long deployCount = graph.getLinks().stream().filter(l -> "deploys".equals(l.getType())).count();
        assertEquals(1, deployCount);
        assertEquals("s-201", graph.getLinks().get(0).getSource());
        assertEquals("p-401", graph.getLinks().get(0).getTarget());
    }

    @Test
    public void getGraph_includes_depends_on_edges() {
        when(operationServerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(operationProjectDeployInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(project(401L, "moli-user-center", 201L)));
        when(operationComponentDeployInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(component(301L, "MySQL", 201L)));
        when(operationServerProjectLinkMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(operationServerComponentLinkMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        OperationProjectComponent pc = new OperationProjectComponent();
        pc.setProjectId(401L);
        pc.setComponentId(301L);
        when(operationProjectComponentLinkMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(pc));
        when(portMatrixProvider.check(any(), any())).thenReturn(new OperationPortMatrixPortCheck(1, "8080", "user-center", "ok"));

        OperationTopologyGraphVo graph = topologyService.getGraph();

        assertEquals(1, graph.getLinks().size());
        assertEquals("depends_on", graph.getLinks().get(0).getType());
        assertEquals("p-401", graph.getLinks().get(0).getSource());
        assertEquals("c-301", graph.getLinks().get(0).getTarget());
    }

    private static OperationServerInfo server(Long id, String name) {
        OperationServerInfo row = new OperationServerInfo();
        row.setId(id);
        row.setServerName(name);
        row.setIp("10.0.0.1");
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
        return row;
    }

    private static OperationComponentDeployInfo component(Long id, String name, Long serverId) {
        OperationComponentDeployInfo row = new OperationComponentDeployInfo();
        row.setId(id);
        row.setComponentName(name);
        row.setServerId(serverId);
        row.setPort("3306");
        row.setEnvironment(1);
        row.setStatus(1);
        return row;
    }
}
