package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.knowledge.server.dto.KbLlmConfigVo;
import com.moli.knowledge.server.dto.KbOpsDashboardVo;
import com.moli.knowledge.server.dto.KbOpsLintSummaryVo;
import com.moli.knowledge.server.dto.KbOpsLlmSummaryVo;
import com.moli.knowledge.server.dto.KbOpsSyncTrendPointVo;
import com.moli.knowledge.server.entity.KbLintIssue;
import com.moli.knowledge.server.entity.KbRelation;
import com.moli.knowledge.server.entity.KbSyncLog;
import com.moli.knowledge.server.mapper.KbLintIssueMapper;
import com.moli.knowledge.server.mapper.KbRelationMapper;
import com.moli.knowledge.server.mapper.KbSyncLogMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbLlmConfigService;
import com.moli.knowledge.server.service.KbOpsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class KbOpsServiceImpl implements KbOpsService {

    private static final int DEFAULT_TREND_DAYS = 7;
    private static final int MAX_TREND_DAYS = 30;
    private static final int TOP_BROKEN = 10;

    @Resource
    private KbAclService kbAclService;
    @Resource
    private KbSyncLogMapper kbSyncLogMapper;
    @Resource
    private KbLintIssueMapper kbLintIssueMapper;
    @Resource
    private KbRelationMapper kbRelationMapper;
    @Resource
    private KbLlmConfigService kbLlmConfigService;

    @Override
    public KbOpsDashboardVo dashboard(Long spaceId, Integer trendDays) {
        kbAclService.assertCanOpsDashboard(spaceId);
        List<Long> scope = resolveScope(spaceId);
        int days = normalizeTrendDays(trendDays);

        KbOpsDashboardVo vo = new KbOpsDashboardVo();
        vo.setSpaceId(spaceId);
        vo.setSyncTrend(buildSyncTrend(scope, days));
        vo.setLintSummary(buildLintSummary(scope));
        vo.setUnresolvedRelationCount(countUnresolvedRelations(scope));
        vo.setLlm(buildLlmSummary());
        return vo;
    }

    private List<Long> resolveScope(Long spaceId) {
        if (spaceId != null) {
            kbAclService.assertCanRead(spaceId);
            return Collections.singletonList(spaceId);
        }
        return kbAclService.accessibleSpaceIds();
    }

    private int normalizeTrendDays(Integer trendDays) {
        if (trendDays == null || trendDays <= 0) {
            return DEFAULT_TREND_DAYS;
        }
        return Math.min(trendDays, MAX_TREND_DAYS);
    }

    private List<KbOpsSyncTrendPointVo> buildSyncTrend(List<Long> scope, int days) {
        LocalDate start = LocalDate.now().minusDays(days - 1L);
        Date from = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<KbSyncLog> wrapper = new LambdaQueryWrapper<KbSyncLog>()
                .ge(KbSyncLog::getCreateTime, from);
        if (scope != null && !scope.isEmpty()) {
            wrapper.in(KbSyncLog::getSpaceId, scope);
        } else {
            return emptyTrend(days);
        }
        List<KbSyncLog> rows = kbSyncLogMapper.selectList(wrapper);

        Map<String, Boolean> batchFailed = new HashMap<>();
        Map<String, Date> batchDate = new HashMap<>();
        for (KbSyncLog row : rows) {
            if (row.getBatchNo() == null) {
                continue;
            }
            batchFailed.merge(row.getBatchNo(),
                    "fail".equalsIgnoreCase(row.getStatus()), Boolean::logicalOr);
            batchDate.putIfAbsent(row.getBatchNo(), row.getCreateTime());
        }

        Map<String, int[]> dayStats = new TreeMap<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        for (Map.Entry<String, Boolean> entry : batchFailed.entrySet()) {
            Date created = batchDate.get(entry.getKey());
            if (created == null) {
                continue;
            }
            String dayKey = fmt.format(created);
            int[] counts = dayStats.computeIfAbsent(dayKey, k -> new int[2]);
            if (Boolean.TRUE.equals(entry.getValue())) {
                counts[1]++;
            } else {
                counts[0]++;
            }
        }

        List<KbOpsSyncTrendPointVo> trend = new ArrayList<>();
        LocalDate cursor = start;
        LocalDate end = LocalDate.now();
        while (!cursor.isAfter(end)) {
            String key = cursor.toString();
            int[] counts = dayStats.getOrDefault(key, new int[2]);
            KbOpsSyncTrendPointVo point = new KbOpsSyncTrendPointVo();
            point.setDate(key);
            point.setSuccessBatches(counts[0]);
            point.setFailBatches(counts[1]);
            trend.add(point);
            cursor = cursor.plusDays(1);
        }
        return trend;
    }

    private List<KbOpsSyncTrendPointVo> emptyTrend(int days) {
        List<KbOpsSyncTrendPointVo> trend = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(days - 1L);
        LocalDate cursor = start;
        while (!cursor.isAfter(LocalDate.now())) {
            KbOpsSyncTrendPointVo point = new KbOpsSyncTrendPointVo();
            point.setDate(cursor.toString());
            trend.add(point);
            cursor = cursor.plusDays(1);
        }
        return trend;
    }

    private KbOpsLintSummaryVo buildLintSummary(List<Long> scope) {
        if (scope == null || scope.isEmpty()) {
            return new KbOpsLintSummaryVo();
        }
        List<KbLintIssue> issues = kbLintIssueMapper.selectList(new LambdaQueryWrapper<KbLintIssue>()
                .in(KbLintIssue::getSpaceId, scope));

        KbOpsLintSummaryVo summary = new KbOpsLintSummaryVo();
        Map<String, Long> openByType = new LinkedHashMap<>();
        List<String> topBroken = new ArrayList<>();

        for (KbLintIssue issue : issues) {
            Integer status = issue.getStatus() == null ? 0 : issue.getStatus();
            if (status == 0) {
                summary.setOpenCount(summary.getOpenCount() + 1);
                String type = issue.getIssueType() == null ? "unknown" : issue.getIssueType();
                openByType.merge(type, 1L, Long::sum);
                if ("broken_link".equals(type) && topBroken.size() < TOP_BROKEN) {
                    topBroken.add(issue.getDetail());
                }
            } else if (status == 1) {
                summary.setIgnoredCount(summary.getIgnoredCount() + 1);
            } else if (status == 2) {
                summary.setFixedCount(summary.getFixedCount() + 1);
            }
        }
        summary.setOpenByType(openByType);
        summary.setTopBrokenLinks(topBroken);
        return summary;
    }

    private long countUnresolvedRelations(List<Long> scope) {
        if (scope == null || scope.isEmpty()) {
            return 0;
        }
        Integer count = kbRelationMapper.selectCount(new LambdaQueryWrapper<KbRelation>()
                .eq(KbRelation::getIsDelete, CommonConstant.UN_DELETE)
                .eq(KbRelation::getResolved, 0)
                .in(KbRelation::getSpaceId, scope));
        return count == null ? 0 : count.longValue();
    }

    private KbOpsLlmSummaryVo buildLlmSummary() {
        KbOpsLlmSummaryVo llm = new KbOpsLlmSummaryVo();
        try {
            KbLlmConfigVo cfg = kbLlmConfigService.getConfig();
            if (cfg != null) {
                llm.setEnabled(cfg.isConfigEnabled());
                llm.setAvailable(cfg.isAvailable());
                llm.setProvider(cfg.getProvider());
                llm.setModel(cfg.getModel());
            }
        } catch (Exception ignored) {
            llm.setAvailable(false);
        }
        return llm;
    }
}
