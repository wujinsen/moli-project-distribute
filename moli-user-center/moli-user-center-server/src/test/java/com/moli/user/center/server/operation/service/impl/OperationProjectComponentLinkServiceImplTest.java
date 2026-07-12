package com.moli.user.center.server.operation.service.impl;

import com.moli.common.core.IdGenerator;
import com.moli.common.core.SnowflakeIdWorker;
import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.vo.OperationProjectComponentLinksVo;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectComponentLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationProjectComponentLinkServiceImplTest {

    @InjectMocks
    private OperationProjectComponentLinkServiceImpl linkService;

    @Mock
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Mock
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;
    @Mock
    private OperationProjectComponentLinkMapper operationProjectComponentLinkMapper;

    @Before
    public void initIdGenerator() throws Exception {
        java.lang.reflect.Field field = IdGenerator.class.getDeclaredField("idWorker");
        field.setAccessible(true);
        field.set(null, new SnowflakeIdWorker(0, 0));
    }

    @Test
    public void getLinks_returns_component_ids() {
        when(operationProjectDeployInfoMapper.selectById(401L)).thenReturn(new OperationProjectDeployInfo());
        when(operationProjectComponentLinkMapper.selectComponentIdsByProjectId(401L))
                .thenReturn(Arrays.asList(301L, 302L));

        OperationProjectComponentLinksVo vo = linkService.getLinks(401L);

        assertEquals(Long.valueOf(401L), vo.getProjectId());
        assertEquals(Arrays.asList(301L, 302L), vo.getComponentIds());
    }

    @Test
    public void syncLinks_inserts_one_row_per_component() {
        when(operationProjectDeployInfoMapper.selectById(401L)).thenReturn(new OperationProjectDeployInfo());
        when(operationComponentDeployInfoMapper.selectById(301L)).thenReturn(new OperationComponentDeployInfo());
        when(operationComponentDeployInfoMapper.selectById(302L)).thenReturn(new OperationComponentDeployInfo());

        linkService.syncLinks(401L, Arrays.asList(302L, 301L));

        verify(operationProjectComponentLinkMapper, times(1)).delete(any());
        verify(operationProjectComponentLinkMapper, times(2)).insert(any());
    }

    @Test
    public void syncLinks_rejects_unknown_component() {
        when(operationProjectDeployInfoMapper.selectById(401L)).thenReturn(new OperationProjectDeployInfo());
        when(operationComponentDeployInfoMapper.selectById(999L)).thenReturn(null);

        try {
            linkService.syncLinks(401L, Collections.singletonList(999L));
            fail("expected unknown component");
        } catch (BaseException ignored) {
        }
    }
}
