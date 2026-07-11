package com.moli.user.center.server.operation.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.vo.OperationComponentLinksVo;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerComponentLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.common.core.IdGenerator;
import com.moli.common.core.SnowflakeIdWorker;
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
public class OperationComponentLinkServiceImplTest {

    @InjectMocks
    private OperationComponentLinkServiceImpl linkService;

    @Mock
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;
    @Mock
    private OperationServerMapper operationServerMapper;
    @Mock
    private OperationServerLinkMapper operationServerLinkMapper;
    @Mock
    private OperationServerComponentLinkMapper operationServerComponentLinkMapper;

    @Before
    public void initIdGenerator() throws Exception {
        java.lang.reflect.Field field = IdGenerator.class.getDeclaredField("idWorker");
        field.setAccessible(true);
        field.set(null, new SnowflakeIdWorker(0, 0));
    }

    @Test
    public void getLinks_returns_server_ids() {
        when(operationComponentDeployInfoMapper.selectById(306L)).thenReturn(new OperationComponentDeployInfo());
        when(operationServerLinkMapper.selectServerIdsByComponentId(306L)).thenReturn(Arrays.asList(201L, 206L));

        OperationComponentLinksVo vo = linkService.getLinks(306L);

        assertEquals(Long.valueOf(306L), vo.getComponentId());
        assertEquals(Arrays.asList(201L, 206L), vo.getServerIds());
    }

    @Test
    public void syncLinks_inserts_one_row_per_server() {
        when(operationComponentDeployInfoMapper.selectById(306L)).thenReturn(new OperationComponentDeployInfo());
        when(operationServerMapper.selectById(201L)).thenReturn(new OperationServerInfo());
        when(operationServerMapper.selectById(206L)).thenReturn(new OperationServerInfo());

        linkService.syncLinks(306L, Arrays.asList(206L, 201L), 201L);

        verify(operationServerComponentLinkMapper, times(1)).delete(any());
        verify(operationServerComponentLinkMapper, times(2)).insert(any());
    }

    @Test
    public void syncLinks_rejects_unknown_server() {
        when(operationComponentDeployInfoMapper.selectById(306L)).thenReturn(new OperationComponentDeployInfo());
        when(operationServerMapper.selectById(999L)).thenReturn(null);

        try {
            linkService.syncLinks(306L, Collections.singletonList(999L), null);
            fail("expected unknown server");
        } catch (BaseException ignored) {
        }
    }
}
