package com.moli.user.center.server.operation.service.impl;

import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.mapper.OperationTaskMapper;
import com.moli.user.center.server.operation.support.OperationCrudSupport;
import com.moli.user.center.server.operation.support.OperationSecretSupport;
import com.moli.user.center.server.operation.support.OperationServerCascadeSupport;
import com.moli.user.center.server.operation.ssh.OperationSshClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationServerServiceImplDeleteTest {

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
    private OperationTaskMapper operationTaskMapper;
    @Mock
    private OperationSecretSupport secretSupport;
    @Mock
    private OperationSshClient sshClient;
    @Mock
    private OperationServerCascadeSupport serverCascadeSupport;

    @Before
    public void noRunningTasks() {
        when(operationTaskMapper.selectCount(any())).thenReturn(0);
    }

    @Test
    public void deleteByIds_cascades_and_deletes_each_server() {
        operationServerService.deleteByIds(new Long[]{204L, 205L});

        verify(serverCascadeSupport, times(1)).onDeleteServer(204L);
        verify(serverCascadeSupport, times(1)).onDeleteServer(205L);
        verify(operationServerMapper, times(1)).deleteById(204L);
        verify(operationServerMapper, times(1)).deleteById(205L);
    }

    @Test
    public void deleteByIds_skips_when_ids_null() {
        operationServerService.deleteByIds(null);

        verify(serverCascadeSupport, times(0)).onDeleteServer(any());
        verify(operationServerMapper, times(0)).deleteById(any());
    }
}
