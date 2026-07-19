package com.moli.knowledge.server.support;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KbGraphExpandSupportTest {

    private static KbGraphExpandConfig baseCfg() {
        return KbGraphExpandConfig.builder()
                .enabled(true)
                .maxHops(2)
                .fanoutPerNode(10)
                .maxNeighbors(20)
                .hopDecay(0.5)
                .inbound(false)
                .includeSameTag(false)
                .build();
    }

    private static Map<Long, Double> expand(
            Map<Long, Double> entries,
            KbGraphExpandConfig cfg,
            List<KbGraphEdge> outbound,
            List<KbGraphEdge> supersedesInbound) {
        return KbGraphExpandSupport.expandBfs(
                entries,
                cfg,
                frontier -> filterOutbound(outbound, frontier),
                frontier -> filterInbound(supersedesInbound, frontier),
                frontier -> Collections.emptyList());
    }

    private static List<KbGraphEdge> filterOutbound(List<KbGraphEdge> edges, Collection<Long> frontier) {
        if (edges == null || edges.isEmpty()) {
            return Collections.emptyList();
        }
        return edges.stream()
                .filter(e -> !e.isInbound() && frontier.contains(e.getSourceDocId()))
                .collect(Collectors.toList());
    }

    private static List<KbGraphEdge> filterInbound(List<KbGraphEdge> edges, Collection<Long> frontier) {
        if (edges == null || edges.isEmpty()) {
            return Collections.emptyList();
        }
        return edges.stream()
                .filter(e -> e.isInbound() && frontier.contains(e.getTargetDocId()))
                .collect(Collectors.toList());
    }

    private static KbGraphEdge out(long source, long target, String type, int weight) {
        return new KbGraphEdge(source, target, type, weight, false);
    }

    private static KbGraphEdge in(long source, long target, String type, int weight) {
        return new KbGraphEdge(source, target, type, weight, true);
    }

    @Test
    public void hopDecayAppliesFromSecondHop() {
        Map<Long, Double> entries = Collections.singletonMap(100L, 1.0);
        List<KbGraphEdge> edges = Arrays.asList(
                out(100L, 200L, "links_to", 1),
                out(200L, 300L, "links_to", 1));
        Map<Long, Double> boost = expand(entries, baseCfg(), edges, Collections.emptyList());
        assertEquals(1.0, boost.get(200L), 1e-9);
        assertEquals(0.5, boost.get(300L), 1e-9);
    }

    @Test
    public void edgeTypeWeightsDiffer() {
        Map<Long, Double> entries = Collections.singletonMap(100L, 1.0);
        List<KbGraphEdge> edges = Arrays.asList(
                out(100L, 200L, "links_to", 1),
                out(100L, 201L, "references", 1));
        Map<Long, Double> boost = expand(entries, baseCfg(), edges, Collections.emptyList());
        assertEquals(1.0, boost.get(200L), 1e-9);
        assertEquals(0.8, boost.get(201L), 1e-9);
    }

    @Test
    public void pathBoostUsesMaxNotSum() {
        Map<Long, Double> entries = Collections.singletonMap(100L, 1.0);
        KbGraphExpandConfig cfg = KbGraphExpandConfig.builder()
                .enabled(true)
                .maxHops(1)
                .fanoutPerNode(10)
                .maxNeighbors(20)
                .hopDecay(0.5)
                .build();
        List<KbGraphEdge> edges = Arrays.asList(
                out(100L, 300L, "links_to", 1),
                out(100L, 300L, "related", 1));
        Map<Long, Double> boost = expand(entries, cfg, edges, Collections.emptyList());
        assertEquals(1.0, boost.get(300L), 1e-9);
    }

    @Test
    public void graphBoostClampedToOne() {
        KbGraphExpandConfig cfg = KbGraphExpandConfig.builder()
                .enabled(true)
                .maxHops(1)
                .fanoutPerNode(5)
                .maxNeighbors(5)
                .hopDecay(0.5)
                .edgeWeights(Collections.singletonMap("links_to", 2.0))
                .build();
        Map<Long, Double> entries = Collections.singletonMap(100L, 1.0);
        List<KbGraphEdge> edges = Collections.singletonList(out(100L, 200L, "links_to", 150));
        Map<Long, Double> boost = expand(entries, cfg, edges, Collections.emptyList());
        assertTrue(boost.get(200L) <= 1.0);
        assertEquals(1.0, boost.get(200L), 1e-9);
    }

    @Test
    public void maxNeighborsCapsBoostMap() {
        KbGraphExpandConfig cfg = KbGraphExpandConfig.builder()
                .enabled(true)
                .maxHops(1)
                .fanoutPerNode(100)
                .maxNeighbors(3)
                .hopDecay(0.5)
                .build();
        Map<Long, Double> entries = Collections.singletonMap(100L, 1.0);
        List<KbGraphEdge> edges = new ArrayList<>();
        for (long i = 200; i < 210; i++) {
            edges.add(out(100L, i, "links_to", 1));
        }
        Map<Long, Double> boost = expand(entries, cfg, edges, Collections.emptyList());
        assertEquals(3, boost.size());
    }

    @Test
    public void maxHopsLimitsDepth() {
        KbGraphExpandConfig cfg = KbGraphExpandConfig.builder()
                .enabled(true)
                .maxHops(1)
                .fanoutPerNode(10)
                .maxNeighbors(20)
                .hopDecay(0.5)
                .build();
        Map<Long, Double> entries = Collections.singletonMap(100L, 1.0);
        List<KbGraphEdge> edges = Arrays.asList(
                out(100L, 200L, "links_to", 1),
                out(200L, 300L, "links_to", 1));
        Map<Long, Double> boost = expand(entries, cfg, edges, Collections.emptyList());
        assertTrue(boost.containsKey(200L));
        assertFalse(boost.containsKey(300L));
    }

    @Test
    public void cycleDoesNotRevisitNodes() {
        Map<Long, Double> entries = Collections.singletonMap(100L, 1.0);
        List<KbGraphEdge> edges = Arrays.asList(
                out(100L, 200L, "links_to", 1),
                out(200L, 100L, "links_to", 1),
                out(200L, 300L, "links_to", 1));
        Map<Long, Double> boost = expand(entries, baseCfg(), edges, Collections.emptyList());
        assertEquals(1.0, boost.get(200L), 1e-9);
        assertEquals(0.5, boost.get(300L), 1e-9);
    }

    @Test
    public void sameTagNotExpandedByDefault() {
        Map<Long, Double> entries = Collections.singletonMap(100L, 1.0);
        List<KbGraphEdge> edges = Collections.singletonList(out(100L, 200L, "same_tag", 1));
        Map<Long, Double> boost = expand(entries, baseCfg(), edges, Collections.emptyList());
        assertTrue(boost.isEmpty());
    }

    @Test
    public void supersedesInboundBringsUpdatePageNotReverse() {
        Map<Long, Double> entries = Collections.singletonMap(10L, 1.0);
        List<KbGraphEdge> outbound = Collections.singletonList(out(20L, 10L, "supersedes", 1));
        List<KbGraphEdge> inbound = Collections.singletonList(in(20L, 10L, "supersedes", 1));
        Map<Long, Double> boost = expand(entries, baseCfg(), outbound, inbound);
        assertFalse(boost.containsKey(10L));
        assertEquals(0.3, boost.get(20L), 1e-9);
    }

    @Test
    public void disabledConfigReturnsEmpty() {
        Map<Long, Double> entries = Collections.singletonMap(100L, 1.0);
        KbGraphExpandConfig cfg = KbGraphExpandConfig.disabled();
        Map<Long, Double> boost = expand(
                entries,
                cfg,
                Collections.singletonList(out(100L, 200L, "links_to", 1)),
                Collections.emptyList());
        assertTrue(boost.isEmpty());
    }

    @Test
    public void resolveEdgeWeightUsesWeightMultiplier() {
        KbGraphExpandConfig cfg = baseCfg();
        KbGraphEdge edge = out(1L, 2L, "references", 100);
        double ew = KbGraphExpandSupport.resolveEdgeWeight(edge, cfg, 100);
        assertEquals(0.8, ew, 1e-9);
    }

    @Test
    public void brokenTargetSkipped() {
        Map<Long, Double> entries = Collections.singletonMap(100L, 1.0);
        List<KbGraphEdge> edges = Collections.singletonList(new KbGraphEdge(100L, null, "links_to", 1, false));
        Map<Long, Double> boost = expand(entries, baseCfg(), edges, Collections.emptyList());
        assertTrue(boost.isEmpty());
    }

    @Test
    public void hubFanInPenaltyReducesBoost() {
        Map<Long, Double> entries = Collections.singletonMap(100L, 1.0);
        List<KbGraphEdge> edges = Collections.singletonList(out(100L, 200L, "links_to", 1));
        KbGraphExpandConfig cfg = KbGraphExpandConfig.builder()
                .enabled(true)
                .maxHops(1)
                .fanoutPerNode(10)
                .maxNeighbors(20)
                .hopDecay(0.5)
                .hubFanInThreshold(15)
                .hubPenalty(0.25)
                .build();
        Map<Long, Double> plain = KbGraphExpandSupport.expandBfs(
                entries, cfg,
                frontier -> edges,
                frontier -> Collections.emptyList(),
                frontier -> Collections.emptyList(),
                ids -> Collections.emptyMap());
        Map<Long, Double> hubbed = KbGraphExpandSupport.expandBfs(
                entries, cfg,
                frontier -> edges,
                frontier -> Collections.emptyList(),
                frontier -> Collections.emptyList(),
                ids -> Collections.singletonMap(200L, 50));
        assertEquals(1.0, plain.get(200L), 1e-9);
        assertEquals(0.25, hubbed.get(200L), 1e-9);
    }
}
