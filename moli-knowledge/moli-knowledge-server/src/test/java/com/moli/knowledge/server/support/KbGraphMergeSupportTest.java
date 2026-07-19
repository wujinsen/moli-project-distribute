package com.moli.knowledge.server.support;

import com.moli.knowledge.server.dto.KbChunkAskRow;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KbGraphMergeSupportTest {

    private static KbGraphExpandConfig mergeCfg() {
        return KbGraphExpandConfig.builder()
                .enabled(true)
                .graphBoostCap(0.5)
                .reinforceFactor(0.5)
                .build();
    }

    private static KbChunkAskRow row(long chunkId, long docId, String slug) {
        KbChunkAskRow r = new KbChunkAskRow();
        r.setChunkId(chunkId);
        r.setDocumentId(docId);
        r.setSpaceId(1L);
        r.setSlug(slug);
        r.setKbType("concept");
        return r;
    }

    @Test
    public void buildEntryDocScoreNorm_topEAndNormalize() {
        Map<Long, Integer> docMax = new HashMap<>();
        docMax.put(10L, 100);
        docMax.put(20L, 50);
        docMax.put(30L, 80);
        Map<Long, Double> norm = KbGraphMergeSupport.buildEntryDocScoreNorm(docMax, 2);
        assertEquals(2, norm.size());
        assertEquals(1.0, norm.get(10L), 1e-9);
        assertEquals(0.8, norm.get(30L), 1e-9);
        assertFalse(norm.containsKey(20L));
    }

    @Test
    public void injectedScore_cappedByGraphBoostCap() {
        assertEquals(50, KbGraphMergeSupport.injectedScore(1.0, 0.5, 100));
        assertEquals(25, KbGraphMergeSupport.injectedScore(0.5, 0.5, 100));
    }

    @Test
    public void mergeGraphIntoPool_injectsNeighborBelowBaseMax() {
        List<Long> ordered = new ArrayList<>(Collections.singletonList(1L));
        Map<Long, Integer> scores = new HashMap<>();
        scores.put(1L, 100);
        Map<Long, KbChunkAskRow> rows = new HashMap<>();
        rows.put(1L, row(1L, 100L, "entry"));
        rows.put(2L, row(2L, 200L, "neighbor"));
        Map<Long, Double> boost = Collections.singletonMap(200L, 1.0);

        KbGraphMergeSupport.mergeGraphIntoPool(ordered, scores, rows, boost, mergeCfg(), r -> 0);

        assertTrue(ordered.contains(2L));
        assertEquals(50, scores.get(2L).intValue());
        assertEquals(100, scores.get(1L).intValue());
        assertEquals(1L, ordered.get(0).longValue());
    }

    @Test
    public void mergeGraphIntoPool_reinforcesDocAlreadyInPool() {
        List<Long> ordered = new ArrayList<>(Arrays.asList(1L, 3L));
        Map<Long, Integer> scores = new HashMap<>();
        scores.put(1L, 100);
        scores.put(3L, 80);
        Map<Long, KbChunkAskRow> rows = new HashMap<>();
        rows.put(1L, row(1L, 100L, "a"));
        rows.put(3L, row(3L, 100L, "a-2"));
        Map<Long, Double> boost = Collections.singletonMap(100L, 0.8);

        KbGraphMergeSupport.mergeGraphIntoPool(ordered, scores, rows, boost, mergeCfg(), r -> 0);

        assertEquals(120, scores.get(1L).intValue());
        assertEquals(100, scores.get(3L).intValue());
    }

    @Test
    public void passesScope_respectsSpaceAndType() {
        KbChunkAskRow ok = row(1L, 1L, "x");
        ok.setSpaceId(5L);
        assertTrue(KbGraphMergeSupport.passesScope(ok, Collections.singletonList(5L),
                Collections.singletonList("concept"), Collections.emptySet()));
        assertFalse(KbGraphMergeSupport.passesScope(ok, Collections.singletonList(6L),
                null, Collections.emptySet()));
    }

    @Test
    public void protectBaseTopK_pinsOriginalPrefixAfterResort() {
        List<Long> ordered = new ArrayList<>(Arrays.asList(1L, 2L, 3L));
        Map<Long, Integer> scores = new HashMap<>();
        scores.put(1L, 100);
        scores.put(2L, 90);
        scores.put(3L, 80);
        Map<Long, KbChunkAskRow> rows = new HashMap<>();
        rows.put(1L, row(1L, 100L, "base-a"));
        rows.put(2L, row(2L, 101L, "base-b"));
        rows.put(3L, row(3L, 102L, "base-c"));
        rows.put(9L, row(9L, 900L, "hub-neighbor"));
        Map<Long, Double> boost = Collections.singletonMap(900L, 1.0);

        KbGraphExpandConfig cfg = KbGraphExpandConfig.builder()
                .enabled(true)
                .graphBoostCap(0.5)
                .reinforceFactor(0.5)
                .protectBaseTopK(3)
                .build();
        // 给邻居超高 term 分，若无 protect 会顶到第一
        KbGraphMergeSupport.mergeGraphIntoPool(ordered, scores, rows, boost, cfg, r -> 1000);

        assertEquals(Arrays.asList(1L, 2L, 3L), ordered.subList(0, 3));
        assertTrue(ordered.contains(9L));
        assertTrue(ordered.indexOf(9L) >= 3);
    }
}
