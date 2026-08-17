package com.moli.user.center.server.operation.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.user.center.server.operation.config.OperationCommandProperties;
import com.moli.user.center.server.operation.service.OperationServerService;
import com.moli.user.center.server.operation.service.OperationTaskService;
import com.moli.user.center.server.operation.ssh.OperationSshClient;
import com.moli.user.center.server.service.ConfigService;
import com.moli.user.center.server.sysparam.ConfigKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationCommandServiceImplTest {

    @InjectMocks
    private OperationCommandServiceImpl commandService;

    @Mock
    private ConfigService configService;
    @Mock
    private OperationServerService operationServerService;
    @Mock
    private OperationTaskService operationTaskService;
    @Mock
    private OperationSshClient sshClient;

    private final OperationCommandProperties commandProperties = new OperationCommandProperties();

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(commandService, "commandProperties", commandProperties);
        commandProperties.setMaxChars(8192);
        commandProperties.setDefaultWorkDir("/opt/moli");
    }

    @Test(expected = BaseException.class)
    public void createCommandTask_rejectsWhenConfigDisabled() {
        when(configService.getBoolean(ConfigKey.OPS_COMMAND_ENABLED)).thenReturn(false);
        // Properties.enabled 故意为 true：证明门禁读的是 ConfigService 而非 Properties 快照
        commandProperties.setEnabled(true);

        commandService.createCommandTask(1L, "echo ok", null);

        verify(operationServerService, never()).requireEntity(org.mockito.ArgumentMatchers.any());
    }

    @Test(expected = BaseException.class)
    public void createCommandTask_rejectsNullServerIdWhenEnabled() {
        when(configService.getBoolean(ConfigKey.OPS_COMMAND_ENABLED)).thenReturn(true);
        commandService.createCommandTask(null, "echo ok", null);
    }
}
