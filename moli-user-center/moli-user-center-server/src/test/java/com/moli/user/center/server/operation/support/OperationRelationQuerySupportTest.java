package com.moli.user.center.server.operation.support;

import com.moli.user.center.common.domain.entity.OperationProjectComponent;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectComponentLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerLinkMapper;
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
}
