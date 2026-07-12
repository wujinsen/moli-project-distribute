package com.moli.user.center.server.operation.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectComponent;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectComponentLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerLinkMapper;
import com.moli.user.center.server.testsupport.MybatisPlusTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationRelationQuerySupportTest {

    @InjectMocks
    private OperationRelationQuerySupport relationQuerySupport;

    @Mock
    private OperationServerLinkMapper operationServerLinkMapper;
    @Mock
    private OperationProjectComponentLinkMapper operationProjectComponentLinkMapper;
    @Mock
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Mock
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;

    @Before
    public void setUp() {
        MybatisPlusTestSupport.initAll();
    }

    @Test
    public void resolveServerIdsForProject_merges_primary_and_nn() {
        when(operationServerLinkMapper.selectServerIdsByProjectId(401L)).thenReturn(Collections.singletonList(202L));

        List<Long> ids = relationQuerySupport.resolveServerIdsForProject(401L, 201L);

        assertEquals(Arrays.asList(202L, 201L), ids);
    }

    @Test
    public void countComponentsByProjectIds_aggregates_rows() {
        OperationProjectComponent row = new OperationProjectComponent();
        row.setProjectId(401L);
        row.setComponentId(301L);
        when(operationProjectComponentLinkMapper.selectList(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Collections.singletonList(row));

        Map<Long, Integer> counts = relationQuerySupport.countComponentsByProjectIds(Collections.singletonList(401L));

        assertEquals(Integer.valueOf(1), counts.get(401L));
    }

    @Test
    public void countServersByProjectIds_includes_primary_when_not_in_nn() {
        when(operationServerLinkMapper.selectServerIdsByProjectId(401L)).thenReturn(Collections.emptyList());

        Map<Long, Integer> counts = relationQuerySupport.countServersByProjectIds(
                Collections.singletonList(401L),
                Collections.singletonMap(401L, 201L));

        assertEquals(Integer.valueOf(1), counts.get(401L));
    }

    @Test
    public void resolveProjectIdsForComponent_reads_nn_table() {
        when(operationProjectComponentLinkMapper.selectProjectIdsByComponentId(301L))
                .thenReturn(Arrays.asList(401L, 402L));

        List<Long> ids = relationQuerySupport.resolveProjectIdsForComponent(301L);

        assertEquals(Arrays.asList(401L, 402L), ids);
    }

    @Test
    public void resolveProjectIdsForServer_merges_nn_and_primary_server_id() {
        when(operationServerLinkMapper.selectProjectIdsByServerId(201L)).thenReturn(Collections.singletonList(402L));

        OperationProjectDeployInfo primary = new OperationProjectDeployInfo();
        primary.setId(401L);
        when(operationProjectDeployInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(primary));

        List<Long> ids = relationQuerySupport.resolveProjectIdsForServer(201L);

        assertEquals(Arrays.asList(402L, 401L), ids);
    }
}
