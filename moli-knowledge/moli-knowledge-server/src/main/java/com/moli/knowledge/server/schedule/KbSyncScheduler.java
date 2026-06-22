package com.moli.knowledge.server.schedule;

import com.moli.knowledge.server.config.KbSyncProperties;
import com.moli.knowledge.server.dto.SyncTriggerVo;
import com.moli.knowledge.server.service.KbSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * kb → MySQL 定时同步（可选，默认关闭）。
 * 开启：{@code kb.sync.schedule-enabled=true}，cron 见 {@code kb.sync.schedule-cron}。
 */
@Slf4j
@Component
public class KbSyncScheduler {

    @Resource
    private KbSyncProperties syncProperties;
    @Resource
    private KbSyncService kbSyncService;

    @Scheduled(cron = "${kb.sync.schedule-cron:0 0 2 * * ?}")
    public void scheduledSync() {
        if (!syncProperties.isEnabled() || !syncProperties.isScheduleEnabled()) {
            return;
        }
        try {
            log.info("定时同步开始 spaceCode={}", syncProperties.getSpaceCode());
            SyncTriggerVo result = kbSyncService.triggerScheduled();
            log.info("定时同步结束 success={} exitCode={}", result.isSuccess(), result.getExitCode());
        } catch (Exception e) {
            log.error("定时同步失败", e);
        }
    }
}
