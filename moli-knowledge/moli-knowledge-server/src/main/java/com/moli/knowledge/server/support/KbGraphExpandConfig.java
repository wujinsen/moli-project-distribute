package com.moli.knowledge.server.support;

import com.moli.knowledge.server.config.KbSearchProperties;
import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * AI-5 §3.1 配置快照（供 {@link KbGraphExpandSupport#expand} 纯算法使用）。
 */
@Value
@Builder
public class KbGraphExpandConfig {

    boolean enabled;
    int maxHops;
    int entryTopE;
    int fanoutPerNode;
    int maxNeighbors;
    int chunksPerNeighbor;
    double hopDecay;
    double graphBoostCap;
    double reinforceFactor;
    int protectBaseTopK;
    int hubFanInThreshold;
    double hubPenalty;
    boolean inbound;
    boolean includeSameTag;
    @Builder.Default
    Map<String, Double> edgeWeights = Collections.emptyMap();
    int queryTimeoutMs;

    public static KbGraphExpandConfig from(KbSearchProperties searchProperties) {
        if (searchProperties == null || searchProperties.getGraph() == null) {
            return disabled();
        }
        KbSearchProperties.Graph g = searchProperties.getGraph();
        return KbGraphExpandConfig.builder()
                .enabled(g.isEnabled())
                .maxHops(g.normalizedMaxHops())
                .entryTopE(g.normalizedEntryTopE())
                .fanoutPerNode(g.normalizedFanoutPerNode())
                .maxNeighbors(g.normalizedMaxNeighbors())
                .chunksPerNeighbor(g.normalizedChunksPerNeighbor())
                .hopDecay(g.normalizedHopDecay())
                .graphBoostCap(g.normalizedGraphBoostCap())
                .reinforceFactor(g.normalizedReinforceFactor())
                .protectBaseTopK(g.normalizedProtectBaseTopK())
                .hubFanInThreshold(g.normalizedHubFanInThreshold())
                .hubPenalty(g.normalizedHubPenalty())
                .inbound(g.isInbound())
                .includeSameTag(g.isIncludeSameTag())
                .edgeWeights(g.resolvedEdgeWeights())
                .queryTimeoutMs(g.normalizedQueryTimeoutMs())
                .build();
    }

    public static KbGraphExpandConfig disabled() {
        return KbGraphExpandConfig.builder().enabled(false).build();
    }

    /** 请求级 graphExpand=true 时强制 enabled，其余字段不变。 */
    public KbGraphExpandConfig withEnabled(boolean on) {
        return KbGraphExpandConfig.builder()
                .enabled(on)
                .maxHops(maxHops)
                .entryTopE(entryTopE)
                .fanoutPerNode(fanoutPerNode)
                .maxNeighbors(maxNeighbors)
                .chunksPerNeighbor(chunksPerNeighbor)
                .hopDecay(hopDecay)
                .graphBoostCap(graphBoostCap)
                .reinforceFactor(reinforceFactor)
                .protectBaseTopK(protectBaseTopK)
                .hubFanInThreshold(hubFanInThreshold)
                .hubPenalty(hubPenalty)
                .inbound(inbound)
                .includeSameTag(includeSameTag)
                .edgeWeights(edgeWeights == null ? Collections.emptyMap() : new HashMap<>(edgeWeights))
                .queryTimeoutMs(queryTimeoutMs)
                .build();
    }

    /** §1.4 默认边权 + {@code edge-weights} 覆盖。 */
    public double edgeWeightFor(String relationType) {
        String type = relationType == null ? "" : relationType.trim().toLowerCase();
        if (edgeWeights != null && edgeWeights.containsKey(type)) {
            return edgeWeights.get(type);
        }
        switch (type) {
            case "links_to":
                return 1.0;
            case "references":
                return 0.8;
            case "related":
                return 0.6;
            case "supersedes":
                return 0.3;
            case "same_tag":
                return includeSameTag ? 0.3 : 0.0;
            default:
                return 0.0;
        }
    }
}
