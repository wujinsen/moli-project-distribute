package com.moli.knowledge.server.service;

import com.moli.knowledge.server.dto.KbOpsLlmCallTrendPointVo;

import java.util.List;
import java.util.Map;

/**
 * KBOPS-9 · LLM 调用审计写入与 Dashboard 聚合。
 */
public interface KbLlmCallLogService {

    void recordSuccess(String scene, Long spaceId, String provider, String model, long latencyMs);

    void recordFail(String scene, Long spaceId, String provider, String model, long latencyMs, String errorMessage);

    /** 近 N 日调用汇总（按 scope 空间过滤）。 */
    LlmCallStats aggregate(List<Long> scopeSpaceIds, boolean includeGlobal, int days);

    class LlmCallStats {
        private long totalCalls;
        private long successCalls;
        private long failCalls;
        private Map<String, Long> callsByScene = new java.util.LinkedHashMap<>();
        private List<KbOpsLlmCallTrendPointVo> callTrend = new java.util.ArrayList<>();

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
    }
}
