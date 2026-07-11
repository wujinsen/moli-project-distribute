package com.moli.user.center.server.operation.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.mapper.OperationTaskMapper;
import com.moli.user.center.server.operation.support.OperationBizException;
import com.moli.user.center.server.operation.support.OperationCrudSupport;
import com.moli.user.center.server.operation.support.OperationSecretSupport;
import com.moli.user.center.server.operation.support.OperationServerCascadeSupport;
import com.moli.user.center.server.operation.ssh.OperationSshClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationServerServiceImplRunningTaskTest {

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

    @Test
    public void deleteByIds_rejects_when_running_task_exists() {
        when(operationTaskMapper.selectCount(any())).thenReturn(1);

        try {
            operationServerService.deleteByIds(new Long[]{204L});
            fail("expected running task rejection");
        } catch (BaseException ex) {
            assertEquals(Integer.valueOf(OperationBizException.CODE_SERVER_TASK_RUNNING), ex.getErrorCode());
        }
    }
}
