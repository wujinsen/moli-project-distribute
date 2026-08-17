package com.moli.user.center.server.operation.schedule;

import com.moli.user.center.server.operation.service.OperationHealthProbeService;
import com.moli.user.center.server.service.ConfigService;
import com.moli.user.center.server.sysparam.ConfigKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class OperationHealthProbeScheduler {

    @Resource
    private ConfigService configService;
    @Resource
    private OperationHealthProbeService operationHealthProbeService;

    @Scheduled(cron = "${ops.health.probe-cron:0 0/15 * * * ?}")
    public void scheduledProbe() {
        // 运行期开关：走 ConfigService（cron 仍用 yaml，属调度节奏冷配置）
        if (!configService.getBoolean(ConfigKey.OPS_HEALTH_PROBE_ENABLED)) {
            return;
        }
        try {
            operationHealthProbeService.probeAll();
        } catch (Exception ex) {
            log.warn("scheduled operation health probe failed: {}", ex.getMessage());
        }
    }
}
