package com.moli.knowledge.server.config;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文档检索模式。默认使用 MySQL ngram 全文索引（表上 ftx_kb_document）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "kb.search")
public class KbSearchProperties {

    /** fulltext：MATCH AGAINST；like：三字段 LIKE（兼容旧行为）。 */
    private String mode = "fulltext";

    /**
     * Query(/kb/ask) 候选页召回上限。开启 fulltext 时先用 ngram 索引按相关度召回
     * 至多 N 页，再做内存 bigram 重排打分，避免把全空间已发布文档全量载入内存。
     */
    private int askCandidateLimit = 100;

    /** true：/kb/ask 按 kb_document_chunk 切段召回；false：整页召回（兼容回退）。 */
    private boolean chunkEnabled = true;

    /** ngram | hybrid | hybrid-rerank（AI-2 §2.1；默认 ngram 零风险）。 */
    private String retrievalStrategy = "ngram";

    private Vector vector = new Vector();
    private Fusion fusion = new Fusion();
    private Rerank rerank = new Rerank();
    private Graph graph = new Graph();

    @Data
    public static class Vector {
        /** sidecar 基址，如 http://127.0.0.1:8099；空则 hybrid 降级 ngram。 */
        private String baseUrl;
        private int topN = 20;
        private int timeoutMs = 1500;
    }

    @Data
    public static class Fusion {
        private int rrfK = 60;
    }

    @Data
    public static class Rerank {
        private int topM = 8;
        /** RRF 融合后进 rerank 的池大小。 */
        private int pool = 30;
        /** cross-encoder 精排超时（毫秒）；独立于 /search，默认 30s。 */
        private int timeoutMs = 30_000;
    }

    /** AI-5 GraphRAG overlay（默认关，G-INV-1）。 */
    @Data
    public static class Graph {
        private boolean enabled = false;
        private int maxHops = 1;
        private int entryTopE = 5;
        private int fanoutPerNode = 5;
        private int maxNeighbors = 20;
        private int chunksPerNeighbor = 2;
        private double hopDecay = 0.5;
        private double graphBoostCap = 0.5;
        private double reinforceFactor = 0.5;
        /** 融合后钉住 base 前缀长度（默认 3；0=关闭）。 */
        private int protectBaseTopK = 3;
        /** 入度超过此值施加 hub 惩罚；≤0 关闭。 */
        private int hubFanInThreshold = 15;
        /** 枢纽 contrib 乘子。 */
        private double hubPenalty = 0.25;
        private boolean inbound = false;
        private boolean includeSameTag = false;
        private Map<String, Double> edgeWeights = new HashMap<>();
        private int queryTimeoutMs = 800;

        public int normalizedMaxHops() {
            int hops = maxHops <= 0 ? 1 : maxHops;
            return Math.min(2, hops);
        }

        public int normalizedEntryTopE() {
            return entryTopE <= 0 ? 5 : entryTopE;
        }

        public int normalizedFanoutPerNode() {
            return fanoutPerNode <= 0 ? 5 : fanoutPerNode;
        }

        public int normalizedMaxNeighbors() {
            return maxNeighbors <= 0 ? 20 : maxNeighbors;
        }

        public int normalizedChunksPerNeighbor() {
            return chunksPerNeighbor <= 0 ? 2 : chunksPerNeighbor;
        }

        public double normalizedHopDecay() {
            return hopDecay <= 0 ? 0.5 : hopDecay;
        }

        public double normalizedGraphBoostCap() {
            return graphBoostCap <= 0 ? 0.5 : Math.min(1.0, graphBoostCap);
        }

        public double normalizedReinforceFactor() {
            return reinforceFactor <= 0 ? 0.5 : Math.min(1.0, reinforceFactor);
        }

        public int normalizedProtectBaseTopK() {
            return Math.max(0, protectBaseTopK);
        }

        public int normalizedHubFanInThreshold() {
            return hubFanInThreshold;
        }

        public double normalizedHubPenalty() {
            if (hubPenalty <= 0) {
                return 0.25;
            }
            return Math.min(1.0, hubPenalty);
        }

        public int normalizedQueryTimeoutMs() {
            return queryTimeoutMs <= 0 ? 800 : queryTimeoutMs;
        }

        /** §1.4 默认表 + YAML {@code edge-weights} 覆盖。 */
        public Map<String, Double> resolvedEdgeWeights() {
            Map<String, Double> defaults = new HashMap<>();
            defaults.put("links_to", 1.0);
            defaults.put("references", 0.8);
            defaults.put("related", 0.6);
            defaults.put("supersedes", 0.3);
            defaults.put("same_tag", includeSameTag ? 0.3 : 0.0);
            if (edgeWeights == null || edgeWeights.isEmpty()) {
                return defaults;
            }
            Map<String, Double> merged = new HashMap<>(defaults);
            for (Map.Entry<String, Double> e : edgeWeights.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    merged.put(e.getKey().trim().toLowerCase(), e.getValue());
                }
            }
            if (!includeSameTag) {
                merged.put("same_tag", 0.0);
            }
            return merged;
        }
    }

    public boolean fullTextEnabled() {
        return "fulltext".equalsIgnoreCase(mode);
    }

    public boolean isChunkEnabled() {
        return chunkEnabled;
    }

    public int normalizedAskCandidateLimit() {
        return askCandidateLimit <= 0 ? 100 : askCandidateLimit;
    }

    public String normalizedRetrievalStrategy() {
        return normalizeStrategy(retrievalStrategy);
    }

    public static String normalizeStrategy(String strategy) {
        if (StringUtils.isBlank(strategy)) {
            return "ngram";
        }
        String s = strategy.trim().toLowerCase();
        if ("hybrid-rerank".equals(s) || "hybrid_rerank".equals(s)) {
            return "hybrid-rerank";
        }
        if ("hybrid".equals(s)) {
            return "hybrid";
        }
        return "ngram";
    }

    public boolean isNgramStrategy(String strategy) {
        return "ngram".equals(normalizeStrategy(strategy));
    }

    public boolean isHybridRerankStrategy(String strategy) {
        return "hybrid-rerank".equals(normalizeStrategy(strategy));
    }

    public boolean hybridSidecarConfigured() {
        return vector != null && StringUtils.isNotBlank(vector.getBaseUrl());
    }

    public int normalizedVectorTopN() {
        return vector == null || vector.getTopN() <= 0 ? 20 : vector.getTopN();
    }

    public int normalizedVectorTimeoutMs() {
        return vector == null || vector.getTimeoutMs() <= 0 ? 1500 : vector.getTimeoutMs();
    }

    public int normalizedRrfK() {
        return fusion == null || fusion.getRrfK() <= 0 ? 60 : fusion.getRrfK();
    }

    public int normalizedRerankTopM() {
        return rerank == null || rerank.getTopM() <= 0 ? 8 : rerank.getTopM();
    }

    public int normalizedRerankPool() {
        return rerank == null || rerank.getPool() <= 0 ? 30 : rerank.getPool();
    }

    public int normalizedRerankTimeoutMs() {
        return rerank == null || rerank.getTimeoutMs() <= 0 ? 30_000 : rerank.getTimeoutMs();
    }

    public boolean graphEnabled() {
        return graph != null && graph.isEnabled();
    }
}
