package com.moli.user.center.server.operation.support;

import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerComponentLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerProjectLinkMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class OperationServerCascadeSupportTest {

    @InjectMocks
    private OperationServerCascadeSupport cascadeSupport;

    @Mock
    private OperationServerProjectLinkMapper operationServerProjectLinkMapper;
    @Mock
    private OperationServerComponentLinkMapper operationServerComponentLinkMapper;
    @Mock
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Mock
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;

    @Test
    public void onDeleteServer_clears_links_and_server_id() {
        cascadeSupport.onDeleteServer(204L);

        verify(operationServerProjectLinkMapper, times(1)).delete(any());
        verify(operationServerComponentLinkMapper, times(1)).delete(any());
        verify(operationProjectDeployInfoMapper, times(1)).update(isNull(), any());
        verify(operationComponentDeployInfoMapper, times(1)).update(isNull(), any());
    }

    @Test
    public void onDeleteProject_removes_nn_row() {
        cascadeSupport.onDeleteProject(401L);
        verify(operationServerProjectLinkMapper, times(1)).delete(any());
    }

    @Test
    public void onDeleteComponent_removes_nn_row() {
        cascadeSupport.onDeleteComponent(301L);
        verify(operationServerComponentLinkMapper, times(1)).delete(any());
    }
}
