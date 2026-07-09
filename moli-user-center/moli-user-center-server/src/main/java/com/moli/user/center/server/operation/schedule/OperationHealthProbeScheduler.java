package com.moli.user.center.server.operation.schedule;

import com.moli.user.center.server.operation.config.OperationHealthProperties;
import com.moli.user.center.server.operation.service.OperationHealthProbeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class OperationHealthProbeScheduler {

    @Resource
    private OperationHealthProperties healthProperties;
    @Resource
    private OperationHealthProbeService operationHealthProbeService;

    @Scheduled(cron = "${ops.health.probe-cron:0 0/15 * * * ?}")
    public void scheduledProbe() {
        if (!healthProperties.isProbeEnabled()) {
            return;
        }
        try {
            operationHealthProbeService.probeAll();
        } catch (Exception ex) {
            log.warn("scheduled operation health probe failed: {}", ex.getMessage());
        }
    }
}
