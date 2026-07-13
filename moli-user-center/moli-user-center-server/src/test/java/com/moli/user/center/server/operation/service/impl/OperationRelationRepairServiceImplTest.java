package com.moli.user.center.server.operation.service.impl;

import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.vo.OperationRelationReconcileVo;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.support.OperationServerBindingSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationRelationRepairServiceImplTest {

    @InjectMocks
    private OperationRelationRepairServiceImpl repairService;

    @Mock
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Mock
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;
    @Mock
    private OperationServerLinkMapper operationServerLinkMapper;
    @Mock
    private OperationServerBindingSupport serverBindingSupport;
    @Mock
    private OperationServerMapper operationServerMapper;

    @Before
    public void setUp() {
        when(operationServerLinkMapper.selectDistinctComponentIdsWithLinks()).thenReturn(Collections.emptyList());
    }

    @Test
    public void reconcilePrimaryServers_fixes_project_when_nn_differs_from_primary() {
        when(operationServerLinkMapper.selectDistinctProjectIdsWithLinks()).thenReturn(Collections.singletonList(401L));
        when(operationServerLinkMapper.selectServerIdsByProjectId(401L)).thenReturn(Collections.singletonList(202L));

        OperationProjectDeployInfo project = new OperationProjectDeployInfo();
        project.setId(401L);
        project.setServerId(201L);
        when(operationProjectDeployInfoMapper.selectById(401L)).thenReturn(project);

        OperationServerInfo window11 = new OperationServerInfo();
        window11.setId(202L);
        window11.setIp("127.0.0.1");
        when(operationServerMapper.selectById(202L)).thenReturn(window11);

        OperationRelationReconcileVo result = repairService.reconcilePrimaryServers();

        assertEquals(1, result.getProjectsFixed());
        assertEquals(0, result.getComponentsFixed());
        assertEquals("project:401 serverId->202", result.getDetails().get(0));
        verify(operationProjectDeployInfoMapper).updateById(project);
        assertEquals(Long.valueOf(202L), project.getServerId());
    }

    @Test
    public void reconcilePrimaryServers_fixes_component_when_nn_differs_from_primary() {
        when(operationServerLinkMapper.selectDistinctProjectIdsWithLinks()).thenReturn(Collections.emptyList());
        when(operationServerLinkMapper.selectDistinctComponentIdsWithLinks()).thenReturn(Collections.singletonList(306L));
        when(operationServerLinkMapper.selectServerIdsByComponentId(306L)).thenReturn(Collections.singletonList(202L));

        OperationComponentDeployInfo component = new OperationComponentDeployInfo();
        component.setId(306L);
        component.setServerId(201L);
        when(operationComponentDeployInfoMapper.selectById(306L)).thenReturn(component);

        OperationServerInfo window11 = new OperationServerInfo();
        window11.setId(202L);
        window11.setIp("127.0.0.1");
        when(operationServerMapper.selectById(202L)).thenReturn(window11);

        OperationRelationReconcileVo result = repairService.reconcilePrimaryServers();

        assertEquals(0, result.getProjectsFixed());
        assertEquals(1, result.getComponentsFixed());
        verify(operationComponentDeployInfoMapper).updateById(component);
        assertEquals(Long.valueOf(202L), component.getServerId());
    }
}
