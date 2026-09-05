package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moli.common.constant.CommonConstant;
import com.moli.knowledge.server.config.KbEvalBaselinesProvider;
import com.moli.knowledge.server.dto.KbLlmConfigVo;
import com.moli.knowledge.server.dto.KbOpsDashboardVo;
import com.moli.knowledge.server.dto.KbOpsEvalRunVo;
import com.moli.knowledge.server.dto.KbOpsEvalStrategySummaryVo;
import com.moli.knowledge.server.dto.KbOpsEvalSummaryVo;
import com.moli.knowledge.server.dto.KbOpsEvalTrendPointVo;
import com.moli.knowledge.server.dto.KbOpsLintSummaryVo;
import com.moli.knowledge.server.dto.KbOpsLlmSummaryVo;
import com.moli.knowledge.server.dto.KbOpsSyncTrendPointVo;
import com.moli.knowledge.server.entity.KbEvalRun;
import com.moli.knowledge.server.entity.KbLintIssue;
import com.moli.knowledge.server.entity.KbRelation;
import com.moli.knowledge.server.entity.KbSyncLog;
import com.moli.knowledge.server.mapper.KbEvalRunMapper;
import com.moli.knowledge.server.mapper.KbLintIssueMapper;
import com.moli.knowledge.server.mapper.KbRelationMapper;
import com.moli.knowledge.server.mapper.KbSyncLogMapper;
import com.moli.knowledge.server.llm.KbLlmConfigSource;
import com.moli.knowledge.server.llm.KbLlmRuntime;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbDriftService;
import com.moli.knowledge.server.service.KbLlmCallLogService;
import com.moli.knowledge.server.service.KbLlmConfigService;
import com.moli.knowledge.server.service.KbOpsService;
import com.moli.knowledge.server.service.KbPlatformLlmConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private static final int DEFAULT_EVAL_TREND_DAYS = 14;
    private static final int MAX_EVAL_TREND_DAYS = 90;
    private static final int DEFAULT_EVAL_RUN_LIMIT = 20;
    private static final int MAX_EVAL_RUN_LIMIT = 100;
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
    @Resource
    private KbLlmCallLogService kbLlmCallLogService;
    @Resource
    private KbPlatformLlmConfigService kbPlatformLlmConfigService;
    @Resource
    private KbLlmRuntime kbLlmRuntime;
    @Resource
    private KbDriftService kbDriftService;
    @Resource
    private KbEvalRunMapper kbEvalRunMapper;
    @Resource
    private KbEvalBaselinesProvider kbEvalBaselinesProvider;

    private static final int DRIFT_DASHBOARD_SAMPLE = 5;

    @Override
    public KbOpsDashboardVo dashboard(Long spaceId, Integer trendDays, boolean includeDrift) {
        kbAclService.assertCanOpsDashboard(spaceId);
        List<Long> scope = resolveScope(spaceId);
        int days = normalizeTrendDays(trendDays);

        KbOpsDashboardVo vo = new KbOpsDashboardVo();
        vo.setSpaceId(spaceId);
        vo.setSyncTrend(buildSyncTrend(scope, days));
        vo.setLintSummary(buildLintSummary(scope));
        vo.setUnresolvedRelationCount(countUnresolvedRelations(scope));
        vo.setLlm(buildLlmSummary(scope, spaceId, days));
        vo.setRetrievalQuality(buildRetrievalQuality());
        if (includeDrift) {
            vo.setDriftSummary(kbDriftService.driftSummary(spaceId, DRIFT_DASHBOARD_SAMPLE));
        } else {
            vo.setDriftSummary(null);
        }
        return vo;
    }

    @Override
    public List<KbOpsEvalTrendPointVo> evalTrend(String strategy, Integer days) {
        kbAclService.assertCanOpsDashboard(null);
        int windowDays = normalizeEvalTrendDays(days);
        Date from = Date.from(LocalDate.now().minusDays(windowDays - 1L)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());

        QueryWrapper<KbEvalRun> wrapper = new QueryWrapper<KbEvalRun>()
                .ge("run_at", from)
                .orderByDesc("run_at");
        if (strategy != null && !strategy.trim().isEmpty()) {
            wrapper.eq("strategy", strategy.trim());
        }
        List<KbEvalRun> rows = kbEvalRunMapper.selectList(wrapper);
        return aggregateEvalTrend(rows, windowDays);
    }

    @Override
    public List<KbOpsEvalRunVo> evalRuns(String strategy, Integer limit) {
        kbAclService.assertCanOpsDashboard(null);
        int cap = normalizeEvalRunLimit(limit);

        QueryWrapper<KbEvalRun> wrapper = new QueryWrapper<KbEvalRun>()
                .orderByDesc("run_at")
                .last("LIMIT " + cap);
        if (strategy != null && !strategy.trim().isEmpty()) {
            wrapper.eq("strategy", strategy.trim());
        }
        List<KbEvalRun> rows = kbEvalRunMapper.selectList(wrapper);
        List<KbOpsEvalRunVo> result = new ArrayList<>(rows.size());
        for (KbEvalRun row : rows) {
            result.add(toEvalRunVo(row));
        }
        return result;
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

    private KbOpsLlmSummaryVo buildLlmSummary(List<Long> scope, Long filterSpaceId, int trendDays) {
        KbOpsLlmSummaryVo llm = new KbOpsLlmSummaryVo();
        llm.setCallLogEnabled(kbPlatformLlmConfigService.isCallLogEnabled());
        llm.setTrendDays(trendDays);
        try {
            KbLlmConfigVo cfg = kbLlmConfigService.getConfig();
            if (cfg != null) {
                llm.setEnabled(cfg.isConfigEnabled());
                llm.setAvailable(cfg.isAvailable());
                llm.setProvider(cfg.getProvider());
                llm.setModel(cfg.getModel());
            }
            KbLlmConfigSource source = kbLlmRuntime.getSource();
            if (source != null) {
                llm.setSource(source.name().toLowerCase());
            }
        } catch (Exception ignored) {
            llm.setAvailable(false);
        }

        if (!kbPlatformLlmConfigService.isCallLogEnabled()) {
            return llm;
        }
        boolean includeGlobal = filterSpaceId == null;
        KbLlmCallLogService.LlmCallStats stats = kbLlmCallLogService.aggregate(scope, includeGlobal, trendDays);
        llm.setTotalCalls(stats.getTotalCalls());
        llm.setSuccessCalls(stats.getSuccessCalls());
        llm.setFailCalls(stats.getFailCalls());
        if (stats.getTotalCalls() > 0) {
            llm.setSuccessRate(stats.getSuccessCalls() * 1.0 / stats.getTotalCalls());
            llm.setFailRate(stats.getFailCalls() * 1.0 / stats.getTotalCalls());
        }
        llm.setCallsByScene(stats.getCallsByScene());
        llm.setCallTrend(stats.getCallTrend());
        if (stats.getSuccessCalls() > 0) {
            llm.setCacheHitRate(stats.getCacheHits() * 1.0 / stats.getSuccessCalls());
        }
        llm.setEstimatedCostUsd(stats.getEstimatedCostUsd().doubleValue());
        llm.setFailoverCount(stats.getFailoverCount());
        llm.setEstimatedCostSavedUsd(stats.getEstimatedCostSavedUsd().doubleValue());
        llm.setEstimatedTokensSaved(stats.getEstimatedTokensSaved());
        llm.setCostTrend(stats.getCostTrend());
        return llm;
    }

    private int normalizeEvalTrendDays(Integer days) {
        if (days == null || days <= 0) {
            return DEFAULT_EVAL_TREND_DAYS;
        }
        return Math.min(days, MAX_EVAL_TREND_DAYS);
    }

    private int normalizeEvalRunLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_EVAL_RUN_LIMIT;
        }
        return Math.min(limit, MAX_EVAL_RUN_LIMIT);
    }

    private KbOpsEvalSummaryVo buildRetrievalQuality() {
        KbOpsEvalSummaryVo summary = new KbOpsEvalSummaryVo();
        Integer baselineGolden = kbEvalBaselinesProvider.goldenTotalFromBaselines();
        if (baselineGolden != null) {
            summary.setGoldenTotal(baselineGolden);
        }

        List<KbOpsEvalStrategySummaryVo> strategies = new ArrayList<>();
        for (String key : kbEvalBaselinesProvider.strategyKeys()) {
            KbEvalRun latest = kbEvalRunMapper.selectOne(new QueryWrapper<KbEvalRun>()
                    .eq("strategy", key)
                    .orderByDesc("run_at")
                    .last("LIMIT 1"));
            KbOpsEvalStrategySummaryVo item = new KbOpsEvalStrategySummaryVo();
            item.setStrategy(key);
            BigDecimal baselineHit3 = kbEvalBaselinesProvider.baselineHit3(key);
            item.setBaselineHit3(baselineHit3);
            if (latest != null) {
                if (summary.getGoldenTotal() == null && latest.getGoldenTotal() != null) {
                    summary.setGoldenTotal(latest.getGoldenTotal());
                }
                item.setLatestRunAt(latest.getRunAt());
                item.setHit1(latest.getHit1());
                item.setHit3(latest.getHit3());
                item.setHit5(latest.getHit5());
                item.setMrr(latest.getMrr());
                item.setP95Ms(latest.getP95Ms());
                item.setErrors(latest.getErrors());
                item.setHasLatestRun(true);
                if (latest.getHit3() != null && baselineHit3 != null) {
                    item.setDeltaHit3(latest.getHit3().subtract(baselineHit3));
                }
                if (latest.getGatePass() != null) {
                    item.setGatePass(latest.getGatePass() == 1);
                } else {
                    item.setGatePass(kbEvalBaselinesProvider.evaluateGate(
                            key, latest.getHit3(), latest.getErrors(), latest.getByDifficultyJson()));
                }
            }
            strategies.add(item);
        }
        summary.setStrategies(strategies);
        return summary;
    }

    private List<KbOpsEvalTrendPointVo> aggregateEvalTrend(List<KbEvalRun> rows, int windowDays) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, KbEvalRun> latestByDayStrategy = new HashMap<>();
        for (KbEvalRun row : rows) {
            if (row.getRunAt() == null) {
                continue;
            }
            String day = fmt.format(row.getRunAt());
            String strat = row.getStrategy() == null ? "" : row.getStrategy();
            String key = day + "\0" + strat;
            latestByDayStrategy.putIfAbsent(key, row);
        }

        List<KbOpsEvalTrendPointVo> points = new ArrayList<>();
        for (Map.Entry<String, KbEvalRun> entry : latestByDayStrategy.entrySet()) {
            KbEvalRun row = entry.getValue();
            KbOpsEvalTrendPointVo point = new KbOpsEvalTrendPointVo();
            point.setDate(fmt.format(row.getRunAt()));
            point.setStrategy(row.getStrategy());
            point.setHit3(row.getHit3());
            point.setMrr(row.getMrr());
            points.add(point);
        }
        points.sort(Comparator.comparing(KbOpsEvalTrendPointVo::getDate)
                .thenComparing(p -> p.getStrategy() == null ? "" : p.getStrategy()));
        return points;
    }

    private KbOpsEvalRunVo toEvalRunVo(KbEvalRun row) {
        KbOpsEvalRunVo vo = new KbOpsEvalRunVo();
        vo.setId(row.getId());
        vo.setRunAt(row.getRunAt());
        vo.setStrategy(row.getStrategy());
        vo.setUseLlm(row.getUseLlm());
        vo.setGoldenTotal(row.getGoldenTotal());
        vo.setAnswerableTotal(row.getAnswerableTotal());
        vo.setNegativeTotal(row.getNegativeTotal());
        vo.setErrors(row.getErrors());
        vo.setHit1(row.getHit1());
        vo.setHit3(row.getHit3());
        vo.setHit5(row.getHit5());
        vo.setHit8(row.getHit8());
        vo.setMrr(row.getMrr());
        vo.setCoverage(row.getCoverage());
        vo.setRefusalAccuracy(row.getRefusalAccuracy());
        vo.setP95Ms(row.getP95Ms());
        vo.setByDifficultyJson(row.getByDifficultyJson());
        vo.setReportPath(row.getReportPath());
        vo.setGitSha(row.getGitSha());
        if (row.getGatePass() != null) {
            vo.setGatePass(row.getGatePass() == 1);
        } else {
            vo.setGatePass(kbEvalBaselinesProvider.evaluateGate(
                    row.getStrategy(), row.getHit3(), row.getErrors(), row.getByDifficultyJson()));
        }
        vo.setCreateTime(row.getCreateTime());
        return vo;
    }
}
