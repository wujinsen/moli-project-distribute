package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.KbOpsLlmCallTrendPointVo;
import com.moli.knowledge.server.dto.KbOpsLlmCostTrendPointVo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * KBOPS-9 · LLM 调用审计写入与 Dashboard 聚合。
 */
public interface KbLlmCallLogService {

    void recordSuccess(String scene, Long spaceId, String provider, String model, long latencyMs);

    default void recordSuccess(String scene, Long spaceId, String provider, String model, long latencyMs,
                               boolean failover) {
        recordSuccess(scene, spaceId, provider, model, latencyMs, failover, false, null, null, null);
    }

    void recordSuccess(String scene, Long spaceId, String provider, String model, long latencyMs,
                       boolean failover, boolean cacheHit,
                       Integer promptTokensEst, Integer completionTokensEst, BigDecimal estimatedCostUsd);

    void recordFail(String scene, Long spaceId, String provider, String model, long latencyMs, String errorMessage);

    default void recordFail(String scene, Long spaceId, String provider, String model, long latencyMs,
                            String errorMessage, boolean failover) {
        recordFail(scene, spaceId, provider, model, latencyMs, errorMessage);
    }

    /** 近 N 日调用汇总（按 scope 空间过滤）。 */
    LlmCallStats aggregate(List<Long> scopeSpaceIds, boolean includeGlobal, int days);

    class LlmCallStats {
        private long totalCalls;
        private long successCalls;
        private long failCalls;
        private long cacheHits;
        private long failoverCount;
        private BigDecimal estimatedCostUsd = BigDecimal.ZERO;
        private BigDecimal estimatedCostSavedUsd = BigDecimal.ZERO;
        private long estimatedTokensSaved;
        private Map<String, Long> callsByScene = new java.util.LinkedHashMap<>();
        private List<KbOpsLlmCallTrendPointVo> callTrend = new java.util.ArrayList<>();
        private List<KbOpsLlmCostTrendPointVo> costTrend = new java.util.ArrayList<>();

        public long getTotalCalls() {
            return totalCalls;
        }

        public void setTotalCalls(long totalCalls) {
            this.totalCalls = totalCalls;
        }

        public long getSuccessCalls() {
            return successCalls;
        }

        public void setSuccessCalls(long successCalls) {
            this.successCalls = successCalls;
        }

        public long getFailCalls() {
            return failCalls;
        }

        public void setFailCalls(long failCalls) {
            this.failCalls = failCalls;
        }

        public long getCacheHits() {
            return cacheHits;
        }

        public void setCacheHits(long cacheHits) {
            this.cacheHits = cacheHits;
        }

        public long getFailoverCount() {
            return failoverCount;
        }

        public void setFailoverCount(long failoverCount) {
            this.failoverCount = failoverCount;
        }

        public BigDecimal getEstimatedCostUsd() {
            return estimatedCostUsd;
        }

        public void setEstimatedCostUsd(BigDecimal estimatedCostUsd) {
            this.estimatedCostUsd = estimatedCostUsd == null ? BigDecimal.ZERO : estimatedCostUsd;
        }

        public BigDecimal getEstimatedCostSavedUsd() {
            return estimatedCostSavedUsd;
        }

        public void setEstimatedCostSavedUsd(BigDecimal estimatedCostSavedUsd) {
            this.estimatedCostSavedUsd = estimatedCostSavedUsd == null ? BigDecimal.ZERO : estimatedCostSavedUsd;
        }

        public long getEstimatedTokensSaved() {
            return estimatedTokensSaved;
        }

        public void setEstimatedTokensSaved(long estimatedTokensSaved) {
            this.estimatedTokensSaved = estimatedTokensSaved;
        }

        public Map<String, Long> getCallsByScene() {
            return callsByScene;
        }

        public void setCallsByScene(Map<String, Long> callsByScene) {
            this.callsByScene = callsByScene;
        }

        public List<KbOpsLlmCallTrendPointVo> getCallTrend() {
            return callTrend;
        }

        public void setCallTrend(List<KbOpsLlmCallTrendPointVo> callTrend) {
            this.callTrend = callTrend;
        }

        public List<KbOpsLlmCostTrendPointVo> getCostTrend() {
            return costTrend;
        }

        public void setCostTrend(List<KbOpsLlmCostTrendPointVo> costTrend) {
            this.costTrend = costTrend;
        }
    }
}
