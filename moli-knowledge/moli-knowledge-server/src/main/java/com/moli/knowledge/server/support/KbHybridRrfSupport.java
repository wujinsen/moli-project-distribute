package com.moli.knowledge.server.support;

import com.moli.knowledge.server.dto.retrieval.VectorSearchHit;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hybrid RRF 融合辅助（AI-2 §2.2 公式 + annex 后处理降权，不改 RRF 公式本身）。
 */
public final class KbHybridRrfSupport {

    /** 与 KbAskServiceImpl.finalizeRecallScore 一致。 */
    public static final double ANNEX_FUSION_SCORE_FACTOR = 1.0 / 3.0;

    private KbHybridRrfSupport() {
    }

    public static boolean isAnnexSlug(String slug) {
        return StringUtils.isNotBlank(slug) && slug.toLowerCase().contains("/annex-");
    }

    public static Map<Long, Double> rrfFuse(List<Long> ngramChunkIds, List<VectorSearchHit> vectorHits, int rrfK) {
        Map<Long, Double> fused = new HashMap<>();
        for (int i = 0; i < ngramChunkIds.size(); i++) {
            Long chunkId = ngramChunkIds.get(i);
            if (chunkId == null) {
                continue;
            }
            fused.merge(chunkId, 1.0 / (rrfK + i + 1), Double::sum);
        }
        if (vectorHits != null) {
            for (VectorSearchHit hit : vectorHits) {
                if (hit.getChunkId() == null) {
                    continue;
                }
                int rank = hit.getRank() <= 0 ? 1 : hit.getRank();
                fused.merge(hit.getChunkId(), 1.0 / (rrfK + rank), Double::sum);
            }
        }
        return fused;
    }

    public static Map<Long, Double> applyAnnexFusionPenalty(Map<Long, Double> fused, Map<Long, String> slugByChunkId) {
        if (fused == null || fused.isEmpty()) {
            return fused;
        }
        Map<Long, Double> adjusted = new HashMap<>(fused.size());
        for (Map.Entry<Long, Double> entry : fused.entrySet()) {
            double score = entry.getValue();
            if (isAnnexSlug(slugByChunkId.get(entry.getKey()))) {
                score *= ANNEX_FUSION_SCORE_FACTOR;
            }
            adjusted.put(entry.getKey(), score);
        }
        return adjusted;
    }

    public static List<Long> sortChunkIdsByRrf(Map<Long, Double> fused) {
        return fused.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public static Map<Long, String> slugIndexFromVectorHits(List<VectorSearchHit> vectorHits) {
        Map<Long, String> slugByChunkId = new HashMap<>();
        if (vectorHits == null) {
            return slugByChunkId;
        }
        for (VectorSearchHit hit : vectorHits) {
            if (hit.getChunkId() != null && StringUtils.isNotBlank(hit.getSlug())) {
                slugByChunkId.putIfAbsent(hit.getChunkId(), hit.getSlug());
            }
        }
        return slugByChunkId;
    }
}