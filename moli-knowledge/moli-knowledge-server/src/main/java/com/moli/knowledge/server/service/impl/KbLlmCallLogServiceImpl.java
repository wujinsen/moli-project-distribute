package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.core.IdGenerator;
import com.moli.knowledge.server.config.KbLlmProperties;
import com.moli.knowledge.server.config.KbLlmRouterProperties;
import com.moli.knowledge.server.dto.KbOpsLlmCallTrendPointVo;
import com.moli.knowledge.server.dto.KbOpsLlmCostTrendPointVo;
import com.moli.knowledge.server.entity.KbLlmCallLog;
import com.moli.knowledge.server.mapper.KbLlmCallLogMapper;
import com.moli.knowledge.server.service.KbLlmCallLogService;
import com.moli.knowledge.server.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
public class KbLlmCallLogServiceImpl implements KbLlmCallLogService {

    private static final String STATUS_SUCCESS = "success";
    private static final String STATUS_FAIL = "fail";

    @Resource
    private KbLlmCallLogMapper kbLlmCallLogMapper;
    @Resource
    private KbLlmProperties kbLlmProperties;
    @Resource
    private KbLlmRouterProperties routerProperties;

    @Override
    public void recordSuccess(String scene, Long spaceId, String provider, String model, long latencyMs) {
        recordSuccess(scene, spaceId, provider, model, latencyMs, false, false, null, null, null);
    }

    @Override
    public void recordSuccess(String scene, Long spaceId, String provider, String model, long latencyMs,
                              boolean failover, boolean cacheHit,
                              Integer promptTokensEst, Integer completionTokensEst, BigDecimal estimatedCostUsd) {
        insert(scene, spaceId, provider, model, STATUS_SUCCESS, latencyMs, null,
                failover, cacheHit, promptTokensEst, completionTokensEst, estimatedCostUsd);
    }

    @Override
    public void recordFail(String scene, Long spaceId, String provider, String model, long latencyMs,
                           String errorMessage) {
        insert(scene, spaceId, provider, model, STATUS_FAIL, latencyMs, errorMessage,
                false, false, null, null, null);
    }

    @Override
    public LlmCallStats aggregate(List<Long> scopeSpaceIds, boolean includeGlobal, int days) {
        LlmCallStats stats = new LlmCallStats();
        int normalizedDays = days <= 0 ? 7 : Math.min(days, 30);
        LocalDate start = LocalDate.now().minusDays(normalizedDays - 1L);
        Date from = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());

        LambdaQueryWrapper<KbLlmCallLog> wrapper = new LambdaQueryWrapper<KbLlmCallLog>()
                .ge(KbLlmCallLog::getCreateTime, from);
        if (scopeSpaceIds != null && !scopeSpaceIds.isEmpty()) {
            if (includeGlobal) {
                wrapper.and(w -> w.in(KbLlmCallLog::getSpaceId, scopeSpaceIds)
                        .or().isNull(KbLlmCallLog::getSpaceId));
            } else {
                wrapper.in(KbLlmCallLog::getSpaceId, scopeSpaceIds);
            }
        }
        List<KbLlmCallLog> rows = kbLlmCallLogMapper.selectList(wrapper);

        Map<String, int[]> dayStats = new TreeMap<>();
        Map<String, CostDayBucket> costDayStats = new TreeMap<>();
        Map<String, Long> byScene = new LinkedHashMap<>();
        long success = 0;
        long fail = 0;
        long cacheHits = 0;
        long failoverCount = 0;
        BigDecimal costSum = BigDecimal.ZERO;
        BigDecimal savedSum = BigDecimal.ZERO;
        long tokensSaved = 0;

        KbLlmRouterProperties.Pricing.Rate rate = routerProperties.getPricing().getDefaultRate();

        for (KbLlmCallLog row : rows) {
            if (STATUS_SUCCESS.equalsIgnoreCase(row.getStatus())) {
                success++;
            } else {
                fail++;
            }
            String scene = row.getScene() == null ? "unknown" : row.getScene();
            byScene.merge(scene, 1L, Long::sum);

            boolean hit = Boolean.TRUE.equals(row.getCacheHit());
            if (hit && STATUS_SUCCESS.equalsIgnoreCase(row.getStatus())) {
                cacheHits++;
                int prompt = row.getPromptTokensEst() == null ? 0 : row.getPromptTokensEst();
                int completion = row.getCompletionTokensEst() == null ? 0 : row.getCompletionTokensEst();
                tokensSaved += (long) prompt + completion;
                savedSum = savedSum.add(estimateCost(prompt, completion, rate));
            }
            if (Boolean.TRUE.equals(row.getFailover()) && STATUS_SUCCESS.equalsIgnoreCase(row.getStatus())) {
                failoverCount++;
            }
            if (row.getEstimatedCostUsd() != null && STATUS_SUCCESS.equalsIgnoreCase(row.getStatus())) {
                costSum = costSum.add(row.getEstimatedCostUsd());
            }

            if (row.getCreateTime() != null) {
                String dayKey = row.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
                int[] counts = dayStats.computeIfAbsent(dayKey, k -> new int[2]);
                if (STATUS_SUCCESS.equalsIgnoreCase(row.getStatus())) {
                    counts[0]++;
                } else {
                    counts[1]++;
                }
                CostDayBucket bucket = costDayStats.computeIfAbsent(dayKey, k -> new CostDayBucket());
                if (STATUS_SUCCESS.equalsIgnoreCase(row.getStatus())) {
                    bucket.calls++;
                    if (hit) {
                        bucket.cacheHits++;
                    }
                    if (row.getEstimatedCostUsd() != null) {
                        bucket.costUsd = bucket.costUsd.add(row.getEstimatedCostUsd());
                    }
                }
            }
        }

        stats.setTotalCalls(success + fail);
        stats.setSuccessCalls(success);
        stats.setFailCalls(fail);
        stats.setCacheHits(cacheHits);
        stats.setFailoverCount(failoverCount);
        stats.setEstimatedCostUsd(costSum);
        stats.setEstimatedCostSavedUsd(savedSum);
        stats.setEstimatedTokensSaved(tokensSaved);
        stats.setCallsByScene(byScene);

        List<KbOpsLlmCallTrendPointVo> trend = new ArrayList<>();
        List<KbOpsLlmCostTrendPointVo> costTrend = new ArrayList<>();
        LocalDate cursor = start;
        LocalDate end = LocalDate.now();
        while (!cursor.isAfter(end)) {
            String key = cursor.toString();
            int[] counts = dayStats.getOrDefault(key, new int[2]);
            KbOpsLlmCallTrendPointVo point = new KbOpsLlmCallTrendPointVo();
            point.setDate(key);
            point.setSuccessCalls(counts[0]);
            point.setFailCalls(counts[1]);
            trend.add(point);

            CostDayBucket bucket = costDayStats.getOrDefault(key, new CostDayBucket());
            KbOpsLlmCostTrendPointVo costPoint = new KbOpsLlmCostTrendPointVo();
            costPoint.setDate(key);
            costPoint.setEstimatedCostUsd(bucket.costUsd.doubleValue());
            costPoint.setCacheHits(bucket.cacheHits);
            costPoint.setCalls(bucket.calls);
            costTrend.add(costPoint);

            cursor = cursor.plusDays(1);
        }
        stats.setCallTrend(trend);
        stats.setCostTrend(costTrend);
        return stats;
    }

    private static BigDecimal estimateCost(int promptTokens, int completionTokens,
                                           KbLlmRouterProperties.Pricing.Rate rate) {
        double cost = (promptTokens * rate.getInputPer1kUsd() + completionTokens * rate.getOutputPer1kUsd()) / 1000.0;
        return BigDecimal.valueOf(cost).setScale(6, RoundingMode.HALF_UP);
    }

    private void insert(String scene, Long spaceId, String provider, String model, String status,
                        long latencyMs, String errorMessage,
                        boolean failover, boolean cacheHit,
                        Integer promptTokensEst, Integer completionTokensEst, BigDecimal estimatedCostUsd) {
        if (!kbLlmProperties.isCallLogEnabled()) {
            return;
        }
        try {
            Date now = new Date();
            KbLlmCallLog row = new KbLlmCallLog();
            row.setId(IdGenerator.getId());
            row.setSpaceId(spaceId);
            row.setUserId(ShiroUtils.getUserId());
            row.setScene(StringUtils.defaultIfBlank(scene, "unknown"));
            row.setProvider(provider);
            row.setModel(model);
            row.setStatus(status);
            row.setLatencyMs((int) Math.min(Integer.MAX_VALUE, latencyMs));
            row.setCacheHit(cacheHit);
            row.setFailover(failover);
            row.setPromptTokensEst(promptTokensEst);
            row.setCompletionTokensEst(completionTokensEst);
            row.setEstimatedCostUsd(estimatedCostUsd);
            if (StringUtils.isNotBlank(errorMessage)) {
                row.setErrorMessage(errorMessage.substring(0, Math.min(512, errorMessage.length())));
            }
            row.setCreateTime(now);
            kbLlmCallLogMapper.insert(row);
        } catch (Exception e) {
            log.warn("kb_llm_call_log 写入失败 scene={}: {}", scene, e.getMessage());
        }
    }

    private static final class CostDayBucket {
        private int calls;
        private int cacheHits;
        private BigDecimal costUsd = BigDecimal.ZERO;
    }
}
