package com.moli.knowledge.server.schedule;

import com.moli.knowledge.server.config.KbLintScanProperties;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbInsightService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * KBOPS-8 · 定时 DB 健康体检落库（可选，默认关闭）。
 */
@Slf4j
@Component
public class KbLintScanScheduler {

    @Resource
    private KbLintScanProperties lintScanProperties;
    @Resource
    private KbInsightService kbInsightService;
    @Resource
    private KbSpaceMapper kbSpaceMapper;

    @Scheduled(cron = "${kb.lint.schedule-cron:0 0 3 ? * MON}")
    public void scheduledScan() {
        if (!lintScanProperties.isScheduleEnabled()) {
            return;
        }
        List<Long> spaceIds = resolveSpaceIds();
        log.info("[lint-scan] 定时体检开始 spaces={}", spaceIds);
        for (Long spaceId : spaceIds) {
            try {
                kbInsightService.scanScheduled(spaceId);
                log.info("[lint-scan] 定时体检完成 spaceId={}", spaceId);
            } catch (Exception e) {
                log.error("[lint-scan] 定时体检失败 spaceId={}", spaceId, e);
            }
        }
    }

    private List<Long> resolveSpaceIds() {
        List<Long> configured = lintScanProperties.getScheduleSpaceIds();
        if (configured != null && !configured.isEmpty()) {
            return configured;
        }
        List<KbSpace> spaces = kbSpaceMapper.selectList(new LambdaQueryWrapper<KbSpace>()
                .eq(KbSpace::getIsDelete, CommonConstant.UN_DELETE));
        List<Long> ids = new ArrayList<>();
        for (KbSpace space : spaces) {
            ids.add(space.getId());
        }
        return ids;
    }
}
