package com.moli.knowledge.server.support;

import com.moli.knowledge.server.dto.KbChunkAskRow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;

/**
 * AI-5 §1.3 Step 1/4 入口归一 + 图分注入/强化（纯函数，可单测）。
 */
public final class KbGraphMergeSupport {

    private KbGraphMergeSupport() {
    }

    /**
     * Step 1：base 文档 top-E → 归一化入口分。
     *
     * @param docMaxScore 每个 doc 在 base 池中的最高 chunk 分
     * @param entryTopE   入口页数量上限
     */
    public static Map<Long, Double> buildEntryDocScoreNorm(Map<Long, Integer> docMaxScore, int entryTopE) {
        if (docMaxScore == null || docMaxScore.isEmpty()) {
            return new HashMap<>();
        }
        int globalMax = docMaxScore.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        if (globalMax <= 0) {
            return new HashMap<>();
        }
        int topE = entryTopE <= 0 ? 5 : entryTopE;
        Map<Long, Double> norm = new LinkedHashMap<>();
        docMaxScore.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(topE)
                .forEach(e -> norm.put(e.getKey(), e.getValue() / (double) globalMax));
        return norm;
    }

    /** §1.3 Step 4：纯图邻居注入分。 */
    public static int injectedScore(double graphBoost, double graphBoostCap, int sMax) {
        if (graphBoost <= 0 || graphBoostCap <= 0 || sMax <= 0) {
            return 0;
        }
        return (int) Math.round(graphBoost * graphBoostCap * sMax);
    }

    /** §1.3 Step 4：已在 base 池 doc 的图强化分。 */
    public static int reinforceScore(double graphBoost, double graphBoostCap, double reinforceFactor, int sMax) {
        if (graphBoost <= 0 || graphBoostCap <= 0 || reinforceFactor <= 0 || sMax <= 0) {
            return 0;
        }
        return (int) Math.round(graphBoost * graphBoostCap * sMax * reinforceFactor);
    }

    /**
     * 将图邻居 chunk 并入 hybrid 候选序；已在池 doc 强化分；按 score 重排 ordered。
     */
    public static void mergeGraphIntoPool(
            List<Long> ordered,
            Map<Long, Integer> chunkScores,
            Map<Long, KbChunkAskRow> rowByChunkId,
            Map<Long, Double> graphBoost,
            KbGraphExpandConfig cfg,
            ToIntFunction<KbChunkAskRow> termScorer) {
        if (ordered == null || chunkScores == null || graphBoost == null || graphBoost.isEmpty() || cfg == null) {
            return;
        }
        int sMax = chunkScores.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        if (sMax <= 0) {
            return;
        }
        double cap = cfg.getGraphBoostCap() <= 0 ? 0.5 : cfg.getGraphBoostCap();
        double reinforce = cfg.getReinforceFactor() <= 0 ? 0.5 : cfg.getReinforceFactor();
        int protectK = Math.max(0, cfg.getProtectBaseTopK());
        List<Long> protectedPrefix = new ArrayList<>();
        if (protectK > 0) {
            int n = Math.min(protectK, ordered.size());
            for (int i = 0; i < n; i++) {
                protectedPrefix.add(ordered.get(i));
            }
        }

        Set<Long> baseDocIds = new HashSet<>();
        for (Long chunkId : ordered) {
            KbChunkAskRow row = rowByChunkId.get(chunkId);
            if (row != null && row.getDocumentId() != null) {
                baseDocIds.add(row.getDocumentId());
            }
        }

        for (Map.Entry<Long, Double> entry : graphBoost.entrySet()) {
            Long docId = entry.getKey();
            double boost = entry.getValue();
            if (docId == null || boost <= 0) {
                continue;
            }
            if (baseDocIds.contains(docId)) {
                reinforceExistingChunks(docId, boost, cap, reinforce, sMax, ordered, chunkScores, rowByChunkId);
            }
        }

        Set<Long> seenChunks = new HashSet<>(ordered);
        for (Map.Entry<Long, Double> entry : graphBoost.entrySet()) {
            Long docId = entry.getKey();
            double boost = entry.getValue();
            if (docId == null || boost <= 0 || baseDocIds.contains(docId)) {
                continue;
            }
            for (KbChunkAskRow row : rowByChunkId.values()) {
                if (!docId.equals(row.getDocumentId()) || row.getChunkId() == null) {
                    continue;
                }
                if (seenChunks.add(row.getChunkId())) {
                    int inject = injectedScore(boost, cap, sMax);
                    int term = termScorer == null ? 0 : termScorer.applyAsInt(row);
                    chunkScores.put(row.getChunkId(), inject + term);
                    ordered.add(row.getChunkId());
                }
            }
        }

        ordered.sort(Comparator.comparing((Long id) -> chunkScores.getOrDefault(id, 0)).reversed()
                .thenComparingLong(id -> id));
        pinProtectedPrefix(ordered, protectedPrefix, chunkScores);
    }

    /**
     * §1.3 Step 4：把融合前 base 前缀钉回结果头部（相对次序不变）。
     */
    static void pinProtectedPrefix(List<Long> ordered, List<Long> protectedPrefix, Map<Long, Integer> chunkScores) {
        if (ordered == null || protectedPrefix == null || protectedPrefix.isEmpty()) {
            return;
        }
        Set<Long> pinned = new HashSet<>();
        List<Long> head = new ArrayList<>();
        for (Long id : protectedPrefix) {
            if (id != null && chunkScores != null && chunkScores.containsKey(id) && pinned.add(id)) {
                head.add(id);
            }
        }
        if (head.isEmpty()) {
            return;
        }
        List<Long> rest = new ArrayList<>();
        for (Long id : ordered) {
            if (!pinned.contains(id)) {
                rest.add(id);
            }
        }
        ordered.clear();
        ordered.addAll(head);
        ordered.addAll(rest);
    }

    private static void reinforceExistingChunks(
            Long docId,
            double graphBoost,
            double cap,
            double reinforce,
            int sMax,
            List<Long> ordered,
            Map<Long, Integer> chunkScores,
            Map<Long, KbChunkAskRow> rowByChunkId) {
        int bonus = reinforceScore(graphBoost, cap, reinforce, sMax);
        if (bonus <= 0) {
            return;
        }
        for (Long chunkId : ordered) {
            KbChunkAskRow row = rowByChunkId.get(chunkId);
            if (row != null && docId.equals(row.getDocumentId())) {
                chunkScores.merge(chunkId, bonus, Integer::sum);
            }
        }
    }

    public static boolean passesScope(KbChunkAskRow row, List<Long> scopeSpaces,
                                      List<String> includeTypes, Set<String> excludeTypes) {
        if (row == null || row.getSpaceId() == null || scopeSpaces == null || !scopeSpaces.contains(row.getSpaceId())) {
            return false;
        }
        if (row.getKbType() != null && excludeTypes != null && excludeTypes.contains(row.getKbType())) {
            return false;
        }
        if (includeTypes != null && !includeTypes.isEmpty() && row.getKbType() != null
                && !includeTypes.contains(row.getKbType())) {
            return false;
        }
        return true;
    }

    public static Map<Long, Integer> docMaxScores(Map<Long, Integer> chunkScores, Map<Long, KbChunkAskRow> rowByChunkId) {
        Map<Long, Integer> docMax = new HashMap<>();
        for (Map.Entry<Long, Integer> e : chunkScores.entrySet()) {
            KbChunkAskRow row = rowByChunkId.get(e.getKey());
            if (row == null || row.getDocumentId() == null) {
                continue;
            }
            docMax.merge(row.getDocumentId(), e.getValue(), Math::max);
        }
        return docMax;
    }
}
