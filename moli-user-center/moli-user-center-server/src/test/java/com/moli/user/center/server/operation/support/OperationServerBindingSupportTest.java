package com.moli.user.center.server.operation.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.server.operation.health.OperationHealthStatus;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationServerBindingSupportTest {

    @InjectMocks
    private OperationServerBindingSupport bindingSupport;

    @Mock
    private OperationServerMapper operationServerMapper;

    @Test
    public void bindProject_fills_ip_from_serverId() {
        OperationServerInfo server = server(201L, "127.0.0.1", "127.0.0.1");
        when(operationServerMapper.selectById(201L)).thenReturn(server);

        OperationProjectDeployInfo row = new OperationProjectDeployInfo();
        row.setServerId(201L);
        bindingSupport.bindProject(row);

        assertEquals("127.0.0.1", row.getServerIp());
        assertEquals("127.0.0.1", row.getInnerIp());
    }

    @Test
    public void bindProject_resolves_serverId_from_ip() {
        OperationServerInfo server = server(204L, "52.62.xxx.xxx", "172.31.30.10");
        when(operationServerMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(server))
                .thenReturn(Collections.emptyList());

        OperationProjectDeployInfo row = new OperationProjectDeployInfo();
        row.setServerIp("52.62.xxx.xxx");
        bindingSupport.bindProject(row);

        assertEquals(Long.valueOf(204L), row.getServerId());
        assertEquals("52.62.xxx.xxx", row.getServerIp());
        assertEquals("172.31.30.10", row.getInnerIp());
    }

    @Test
    public void bindProject_picks_up_server_when_duplicate_ip() {
        OperationServerInfo down = server(731873612079824896L, "152.136.254.78", "127.0.0.2");
        down.setStatus(OperationHealthStatus.DOWN);
        OperationServerInfo up = server(201L, "152.136.254.78", "127.0.0.2");
        up.setStatus(OperationHealthStatus.UP);
        when(operationServerMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(down, up));

        OperationProjectDeployInfo row = new OperationProjectDeployInfo();
        row.setServerIp("152.136.254.78");
        bindingSupport.bindProject(row);

        assertEquals(Long.valueOf(201L), row.getServerId());
    }

    @Test
    public void bindProject_overwrites_stale_ip_when_serverId_set() {
        when(operationServerMapper.selectById(201L)).thenReturn(server(201L, "127.0.0.2", "127.0.0.2"));

        OperationProjectDeployInfo row = new OperationProjectDeployInfo();
        row.setServerId(201L);
        row.setServerIp("127.0.0.1");
        bindingSupport.bindProject(row);

        assertEquals("127.0.0.2", row.getServerIp());
        assertEquals("127.0.0.2", row.getInnerIp());
    }

    @Test
    public void bindComponent_fills_ip_from_serverId() {
        when(operationServerMapper.selectById(205L)).thenReturn(server(205L, "10.0.3.20", "172.31.30.20"));

        OperationComponentDeployInfo row = new OperationComponentDeployInfo();
        row.setServerId(205L);
        bindingSupport.bindComponent(row);

        assertEquals("10.0.3.20", row.getServerIp());
    }

    @Test
    public void bindComponent_leaves_unmatched_ip_without_serverId() {
        when(operationServerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        OperationComponentDeployInfo row = new OperationComponentDeployInfo();
        row.setServerIp("192.168.1.99");
        bindingSupport.bindComponent(row);

        assertNull(row.getServerId());
        assertEquals("192.168.1.99", row.getServerIp());
    }

    private OperationServerInfo server(Long id, String ip, String innerIp) {
        OperationServerInfo server = new OperationServerInfo();
        server.setId(id);
        server.setIp(ip);
        server.setInnerIp(innerIp);
        return server;
    }
}
