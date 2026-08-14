package com.moli.knowledge.server.support;

import com.moli.knowledge.server.dto.KbDocFanInCount;
import com.moli.knowledge.server.entity.KbRelation;
import com.moli.knowledge.server.mapper.KbRelationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AI-5 §1.3 Step 2 BFS 扩跳 + §1.4 边权；只产出 graphBoost，不做 chunk 融合（Phase B）。
 */
@Slf4j
@Component
public class KbGraphExpandSupport {

    private final KbRelationMapper relationMapper;

    public KbGraphExpandSupport(KbRelationMapper relationMapper) {
        this.relationMapper = relationMapper;
    }

    /**
     * @param entryDocScoreNorm Step 1 入口 doc → 归一化 base 分（仅 E）
     * @return 邻居 docId → graphBoost（钳 ≤ 1.0）；不含入口 doc
     */
    public Map<Long, Double> expand(Map<Long, Double> entryDocScoreNorm,
                                    List<Long> scopeSpaces,
                                    KbGraphExpandConfig cfg) {
        if (cfg == null || !cfg.isEnabled() || CollectionUtils.isEmpty(entryDocScoreNorm)) {
            return Collections.emptyMap();
        }
        try {
            return expandBfs(
                    entryDocScoreNorm,
                    cfg,
                    frontier -> toEdges(relationMapper.selectBySourceDocIds(frontier, scopeSpaces), false),
                    frontier -> toEdges(relationMapper.selectSupersedesByTargetDocIds(frontier, scopeSpaces), true),
                    frontier -> cfg.isInbound()
                            ? toEdges(relationMapper.selectInboundByTargetDocIds(frontier, scopeSpaces), true)
                            : Collections.emptyList(),
                    docIds -> loadFanIn(docIds, scopeSpaces));
        } catch (Exception ex) {
            log.warn("kb graph expand failed, degrade to empty boost: {}", ex.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<Long, Integer> loadFanIn(Collection<Long> docIds, List<Long> scopeSpaces) {
        if (CollectionUtils.isEmpty(docIds)) {
            return Collections.emptyMap();
        }
        List<KbDocFanInCount> rows = relationMapper.countInboundByTargetDocIds(docIds, scopeSpaces);
        if (CollectionUtils.isEmpty(rows)) {
            return Collections.emptyMap();
        }
        Map<Long, Integer> map = new HashMap<>();
        for (KbDocFanInCount row : rows) {
            if (row.getTargetDocId() != null && row.getCnt() != null) {
                map.put(row.getTargetDocId(), row.getCnt());
            }
        }
        return map;
    }

    /**
     * 单测入口：按 hop 动态供边，不访问 DB（无 hub 入度 → 不惩罚）。
     */
    static Map<Long, Double> expandBfs(
            Map<Long, Double> entryDocScoreNorm,
            KbGraphExpandConfig cfg,
            Function<Collection<Long>, List<KbGraphEdge>> outboundLoader,
            Function<Collection<Long>, List<KbGraphEdge>> supersedesInboundLoader,
            Function<Collection<Long>, List<KbGraphEdge>> inboundLoader) {
        return expandBfs(entryDocScoreNorm, cfg, outboundLoader, supersedesInboundLoader, inboundLoader,
                ids -> Collections.emptyMap());
    }

    /**
     * 单测/生产共用 BFS；{@code fanInLoader} 提供目标入度供 hub 惩罚。
     */
    static Map<Long, Double> expandBfs(
            Map<Long, Double> entryDocScoreNorm,
            KbGraphExpandConfig cfg,
            Function<Collection<Long>, List<KbGraphEdge>> outboundLoader,
            Function<Collection<Long>, List<KbGraphEdge>> supersedesInboundLoader,
            Function<Collection<Long>, List<KbGraphEdge>> inboundLoader,
            Function<Collection<Long>, Map<Long, Integer>> fanInLoader) {
        if (cfg == null || !cfg.isEnabled() || CollectionUtils.isEmpty(entryDocScoreNorm)) {
            return Collections.emptyMap();
        }

        Map<Long, Double> graphBoost = new HashMap<>();
        Map<Long, Long> rootOf = new HashMap<>();
        Set<Long> visited = new HashSet<>();
        List<Long> frontier = new ArrayList<>();

        for (Map.Entry<Long, Double> entry : entryDocScoreNorm.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            visited.add(entry.getKey());
            rootOf.put(entry.getKey(), entry.getKey());
            frontier.add(entry.getKey());
        }
        if (frontier.isEmpty()) {
            return Collections.emptyMap();
        }

        int maxHops = Math.min(2, cfg.getMaxHops() <= 0 ? 1 : cfg.getMaxHops());
        int fanoutPerNode = cfg.getFanoutPerNode() <= 0 ? 5 : cfg.getFanoutPerNode();
        int maxNeighbors = cfg.getMaxNeighbors() <= 0 ? 20 : cfg.getMaxNeighbors();
        double hopDecay = cfg.getHopDecay() <= 0 ? 0.5 : cfg.getHopDecay();

        for (int hop = 1; hop <= maxHops; hop++) {
            if (frontier.isEmpty() || graphBoost.size() >= maxNeighbors) {
                break;
            }

            List<KbGraphEdge> hopEdges = new ArrayList<>();
            hopEdges.addAll(outboundLoader.apply(frontier));
            hopEdges.addAll(supersedesInboundLoader.apply(frontier));
            if (cfg.isInbound()) {
                hopEdges.addAll(inboundLoader.apply(frontier));
            }
            int maxWeightNorm = resolveMaxWeightNorm(hopEdges);

            Set<Long> neighborIds = new HashSet<>();
            for (KbGraphEdge edge : hopEdges) {
                if (edge.isInbound()) {
                    if (edge.getSourceDocId() != null) {
                        neighborIds.add(edge.getSourceDocId());
                    }
                } else if (edge.getTargetDocId() != null) {
                    neighborIds.add(edge.getTargetDocId());
                }
            }
            Map<Long, Integer> fanIn = fanInLoader == null
                    ? Collections.emptyMap()
                    : fanInLoader.apply(neighborIds);

            Map<Long, Double> nextCandidates = new HashMap<>();
            Map<Long, Long> nextRoots = new HashMap<>();
            double hopFactor = Math.pow(hopDecay, hop - 1);

            for (KbGraphEdge edge : hopEdges) {
                if (edge.isInbound()) {
                    applyInboundEdge(edge, frontier, visited, entryDocScoreNorm, rootOf, cfg,
                            maxWeightNorm, hopFactor, fanIn, graphBoost, nextCandidates, nextRoots, maxNeighbors);
                } else {
                    applyOutboundEdge(edge, visited, entryDocScoreNorm, rootOf, cfg,
                            maxWeightNorm, hopFactor, fanIn, graphBoost, nextCandidates, nextRoots, maxNeighbors);
                }
            }

            if (nextCandidates.isEmpty()) {
                break;
            }

            int limit = fanoutPerNode * frontier.size();
            List<Long> nextFrontier = topByScore(nextCandidates, limit);
            for (Long node : nextFrontier) {
                visited.add(node);
                rootOf.put(node, nextRoots.getOrDefault(node, node));
            }
            frontier = nextFrontier;

            if (graphBoost.size() >= maxNeighbors) {
                break;
            }
        }

        entryDocScoreNorm.keySet().forEach(graphBoost::remove);
        return graphBoost;
    }

    private static void applyOutboundEdge(
            KbGraphEdge edge,
            Set<Long> visited,
            Map<Long, Double> entryDocScoreNorm,
            Map<Long, Long> rootOf,
            KbGraphExpandConfig cfg,
            int maxWeightNorm,
            double hopFactor,
            Map<Long, Integer> fanIn,
            Map<Long, Double> graphBoost,
            Map<Long, Double> nextCandidates,
            Map<Long, Long> nextRoots,
            int maxNeighbors) {
        Long source = edge.getSourceDocId();
        Long target = edge.getTargetDocId();
        if (source == null || target == null || visited.contains(target)) {
            return;
        }
        String type = normalizeType(edge.getRelationType());
        if ("supersedes".equals(type)) {
            return;
        }
        if (graphBoost.size() >= maxNeighbors && !graphBoost.containsKey(target)) {
            return;
        }
        double ew = resolveEdgeWeight(edge, cfg, maxWeightNorm);
        if (ew <= 0) {
            return;
        }
        Long root = rootOf.getOrDefault(source, source);
        Double baseNorm = entryDocScoreNorm.get(root);
        if (baseNorm == null || baseNorm <= 0) {
            return;
        }
        double contrib = applyHubPenalty(baseNorm * ew * hopFactor, target, fanIn, cfg);
        mergeBoost(graphBoost, target, contrib);
        nextCandidates.merge(target, graphBoost.get(target), Math::max);
        nextRoots.putIfAbsent(target, root);
    }

    private static void applyInboundEdge(
            KbGraphEdge edge,
            Collection<Long> frontier,
            Set<Long> visited,
            Map<Long, Double> entryDocScoreNorm,
            Map<Long, Long> rootOf,
            KbGraphExpandConfig cfg,
            int maxWeightNorm,
            double hopFactor,
            Map<Long, Integer> fanIn,
            Map<Long, Double> graphBoost,
            Map<Long, Double> nextCandidates,
            Map<Long, Long> nextRoots,
            int maxNeighbors) {
        Long source = edge.getSourceDocId();
        Long target = edge.getTargetDocId();
        if (source == null || target == null || visited.contains(source)) {
            return;
        }
        if (!frontier.contains(target)) {
            return;
        }
        if (graphBoost.size() >= maxNeighbors && !graphBoost.containsKey(source)) {
            return;
        }
        double ew = resolveEdgeWeight(edge, cfg, maxWeightNorm);
        if (ew <= 0) {
            return;
        }
        Long root = rootOf.getOrDefault(target, target);
        Double baseNorm = entryDocScoreNorm.get(root);
        if (baseNorm == null || baseNorm <= 0) {
            return;
        }
        // inbound：邻居是 source（更新页 / 入边对端）
        double contrib = applyHubPenalty(baseNorm * ew * hopFactor, source, fanIn, cfg);
        mergeBoost(graphBoost, source, contrib);
        nextCandidates.merge(source, graphBoost.get(source), Math::max);
        nextRoots.putIfAbsent(source, root);
    }

    static double applyHubPenalty(double contrib, Long docId, Map<Long, Integer> fanIn, KbGraphExpandConfig cfg) {
        if (contrib <= 0 || docId == null || cfg == null || fanIn == null) {
            return contrib;
        }
        int threshold = cfg.getHubFanInThreshold();
        if (threshold <= 0) {
            return contrib;
        }
        int degree = fanIn.getOrDefault(docId, 0);
        if (degree <= threshold) {
            return contrib;
        }
        double penalty = cfg.getHubPenalty() <= 0 ? 0.25 : Math.min(1.0, cfg.getHubPenalty());
        return contrib * penalty;
    }

    private static void mergeBoost(Map<Long, Double> graphBoost, Long docId, double contrib) {
        double prev = graphBoost.getOrDefault(docId, 0.0);
        graphBoost.put(docId, Math.min(1.0, Math.max(prev, contrib)));
    }

    static double resolveEdgeWeight(KbGraphEdge edge, KbGraphExpandConfig cfg, int maxWeightNorm) {
        double ew = cfg.edgeWeightFor(edge.getRelationType());
        if (ew <= 0) {
            return 0.0;
        }
        int weight = edge.getWeight() == null ? 0 : edge.getWeight();
        if (weight <= 0 || maxWeightNorm <= 0) {
            return ew;
        }
        double multiplier = Math.max(0.5, Math.min(1.5, weight / (double) maxWeightNorm));
        return ew * multiplier;
    }

    static int resolveMaxWeightNorm(List<KbGraphEdge> edges) {
        int max = 0;
        for (KbGraphEdge edge : edges) {
            if (edge.getWeight() != null && edge.getWeight() > max) {
                max = edge.getWeight();
            }
        }
        return max > 0 ? max : 100;
    }

    static List<Long> topByScore(Map<Long, Double> scores, int limit) {
        if (scores.isEmpty()) {
            return Collections.emptyList();
        }
        int cap = limit <= 0 ? scores.size() : limit;
        return scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(cap)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static String normalizeType(String relationType) {
        return relationType == null ? "" : relationType.trim().toLowerCase(Locale.ROOT);
    }

    private static List<KbGraphEdge> toEdges(List<KbRelation> relations, boolean inbound) {
        if (CollectionUtils.isEmpty(relations)) {
            return Collections.emptyList();
        }
        List<KbGraphEdge> edges = new ArrayList<>(relations.size());
        for (KbRelation relation : relations) {
            edges.add(inbound ? KbGraphEdge.inbound(relation) : KbGraphEdge.outbound(relation));
        }
        return edges;
    }
}
