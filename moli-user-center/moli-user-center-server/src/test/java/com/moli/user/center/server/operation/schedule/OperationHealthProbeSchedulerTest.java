package com.moli.user.center.server.operation.schedule;

import com.moli.user.center.server.operation.service.OperationHealthProbeService;
import com.moli.user.center.server.service.ConfigService;
import com.moli.user.center.server.sysparam.ConfigKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationHealthProbeSchedulerTest {

    @InjectMocks
    private OperationHealthProbeScheduler scheduler;

    @Mock
    private ConfigService configService;
    @Mock
    private OperationHealthProbeService operationHealthProbeService;

    @Test
    public void scheduledProbe_skipsWhenConfigDisabled() {
        when(configService.getBoolean(ConfigKey.OPS_HEALTH_PROBE_ENABLED)).thenReturn(false);
        scheduler.scheduledProbe();
        verify(operationHealthProbeService, never()).probeAll();
    }

    @Test
    public void scheduledProbe_runsWhenConfigEnabled() {
        when(configService.getBoolean(ConfigKey.OPS_HEALTH_PROBE_ENABLED)).thenReturn(true);
        scheduler.scheduledProbe();
        verify(operationHealthProbeService).probeAll();
    }
}
