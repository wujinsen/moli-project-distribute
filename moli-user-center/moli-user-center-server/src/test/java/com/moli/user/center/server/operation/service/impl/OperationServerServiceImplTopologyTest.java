package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.vo.OperationServerTopologyVo;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.mapper.OperationTaskMapper;
import com.moli.user.center.server.operation.ssh.OperationSshClient;
import com.moli.user.center.server.operation.support.OperationCrudSupport;
import com.moli.user.center.server.operation.support.OperationSecretSupport;
import com.moli.user.center.server.operation.support.OperationServerCascadeSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationServerServiceImplTopologyTest {

    @InjectMocks
    private OperationServerServiceImpl operationServerService;

    @Spy
    private OperationCrudSupport crudSupport = new OperationCrudSupport();

    @Mock
    private OperationServerMapper operationServerMapper;
    @Mock
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Mock
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;
    @Mock
    private OperationServerLinkMapper operationServerLinkMapper;
    @Mock
    private OperationSecretSupport secretSupport;
    @Mock
    private OperationSshClient sshClient;
    @Mock
    private OperationServerCascadeSupport serverCascadeSupport;
    @Mock
    private OperationTaskMapper operationTaskMapper;

    @Test
    public void getTopology_loads_components_matching_public_or_inner_ip() {
        OperationServerInfo server = new OperationServerInfo();
        server.setId(201L);
        server.setServerName("moli-backend-pro");
        server.setIp("203.0.113.10");
        server.setInnerIp("10.0.0.5");
        when(operationServerMapper.selectById(201L)).thenReturn(server);
        when(operationServerLinkMapper.selectComponentIdsByServerId(201L)).thenReturn(Collections.emptyList());
        when(operationServerLinkMapper.selectProjectIdsByServerId(201L)).thenReturn(Collections.emptyList());
        when(operationProjectDeployInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        OperationComponentDeployInfo byPublicIp = component(306L, "Redis", "203.0.113.10");
        OperationComponentDeployInfo byInnerIp = component(307L, "MySQL", "10.0.0.5");
        when(operationComponentDeployInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList())
                .thenReturn(Arrays.asList(byPublicIp, byInnerIp));
        when(operationComponentDeployInfoMapper.selectBatchIds(any()))
                .thenReturn(Arrays.asList(byPublicIp, byInnerIp));

        OperationServerTopologyVo topology = operationServerService.getTopology(201L);

        assertEquals(2, topology.getComponents().size());
        assertTrue(topology.getComponents().stream().anyMatch(c -> Long.valueOf(306L).equals(c.getId())));
        assertTrue(topology.getComponents().stream().anyMatch(c -> Long.valueOf(307L).equals(c.getId())));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<LambdaQueryWrapper<OperationComponentDeployInfo>> captor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(operationComponentDeployInfoMapper, org.mockito.Mockito.atLeastOnce()).selectList(captor.capture());
    }

    private static OperationComponentDeployInfo component(Long id, String name, String serverIp) {
        OperationComponentDeployInfo row = new OperationComponentDeployInfo();
        row.setId(id);
        row.setComponentName(name);
        row.setServerIp(serverIp);
        return row;
    }
}
