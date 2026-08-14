package com.moli.user.center.server.operation.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.vo.OperationProjectLinksVo;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.mapper.OperationServerProjectLinkMapper;
import com.moli.user.center.server.operation.support.OperationServerBindingSupport;
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
public class OperationProjectLinkServiceImplTest {

    @InjectMocks
    private OperationProjectLinkServiceImpl linkService;

    @Mock
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Mock
    private OperationServerMapper operationServerMapper;
    @Mock
    private OperationServerLinkMapper operationServerLinkMapper;
    @Mock
    private OperationServerProjectLinkMapper operationServerProjectLinkMapper;
    @Mock
    private OperationServerBindingSupport serverBindingSupport;

    @Before
    public void initIdGenerator() throws Exception {
        java.lang.reflect.Field field = IdGenerator.class.getDeclaredField("idWorker");
        field.setAccessible(true);
        field.set(null, new SnowflakeIdWorker(0, 0));
    }

    @Test
    public void getLinks_returns_server_ids() {
        when(operationProjectDeployInfoMapper.selectById(401L)).thenReturn(new OperationProjectDeployInfo());
        when(operationServerLinkMapper.selectServerIdsByProjectId(401L)).thenReturn(Arrays.asList(201L, 202L));

        OperationProjectLinksVo vo = linkService.getLinks(401L);

        assertEquals(Long.valueOf(401L), vo.getProjectId());
        assertEquals(Arrays.asList(201L, 202L), vo.getServerIds());
    }

    @Test
    public void saveLinks_syncs_primary_server_id() {
        OperationProjectDeployInfo project = new OperationProjectDeployInfo();
        project.setId(401L);
        project.setServerId(202L);
        when(operationProjectDeployInfoMapper.selectById(401L)).thenReturn(project);
        when(operationServerMapper.selectById(201L)).thenReturn(server(201L, "127.0.0.1"));

        OperationProjectLinksVo links = new OperationProjectLinksVo();
        links.setServerIds(Collections.singletonList(201L));
        linkService.saveLinks(401L, links);

        assertEquals(Long.valueOf(201L), project.getServerId());
        verify(serverBindingSupport).bindProject(project);
        verify(operationProjectDeployInfoMapper).updateById(project);
    }

    @Test
    public void syncLinks_inserts_one_row_per_server() {
        when(operationProjectDeployInfoMapper.selectById(401L)).thenReturn(new OperationProjectDeployInfo());
        when(operationServerMapper.selectById(201L)).thenReturn(new OperationServerInfo());
        when(operationServerMapper.selectById(202L)).thenReturn(new OperationServerInfo());

        linkService.syncLinks(401L, Arrays.asList(202L, 201L), 201L);

        verify(operationServerProjectLinkMapper, times(1)).delete(any());
        verify(operationServerProjectLinkMapper, times(2)).insert(any());
    }

    @Test
    public void syncLinks_rejects_unknown_server() {
        when(operationProjectDeployInfoMapper.selectById(401L)).thenReturn(new OperationProjectDeployInfo());
        when(operationServerMapper.selectById(999L)).thenReturn(null);

        try {
            linkService.syncLinks(401L, Collections.singletonList(999L), null);
            fail("expected unknown server");
        } catch (BaseException ignored) {
        }
    }

    private static OperationServerInfo server(Long id, String ip) {
        OperationServerInfo server = new OperationServerInfo();
        server.setId(id);
        server.setIp(ip);
        return server;
    }
}
