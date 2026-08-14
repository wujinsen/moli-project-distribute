package com.moli.user.center.server.operation.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.dto.operation.OperationServerSaveRequest;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.support.OperationBizException;
import com.moli.user.center.server.operation.support.OperationServerCascadeSupport;
import com.moli.user.center.server.operation.support.OperationSecretSupport;
import com.moli.user.center.server.operation.ssh.OperationSshClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationServerServiceImplDuplicateIpTest {

    @InjectMocks
    private OperationServerServiceImpl operationServerService;

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

    @Test
    public void create_rejects_duplicate_ip_in_same_environment() {
        when(operationServerMapper.selectCount(any())).thenReturn(1);

        OperationServerSaveRequest req = new OperationServerSaveRequest();
        req.setServerName("dup");
        req.setIp("10.0.0.1");
        req.setEnvironment(1);

        try {
            operationServerService.create(req);
            fail("expected duplicate ip");
        } catch (BaseException ex) {
            assertEquals(Integer.valueOf(OperationBizException.CODE_DUPLICATE_IP), ex.getErrorCode());
        }
    }
}
