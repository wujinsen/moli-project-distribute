package com.moli.knowledge.server.service.impl;

import com.moli.knowledge.server.dto.AskResponse;
import com.moli.knowledge.server.dto.KbChunkAskRow;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KbAgenticAskServiceImplTest {

    @Test
    public void parseRewriteJson_extractsRewrittenAndSubQuestions() {
        String raw = "```json\n{\"rewritten\":\"规范问\",\"multiHop\":true,"
                + "\"subQuestions\":[\"子问A\",\"子问B\"]}\n```";
        KbAgenticAskServiceImpl.RewriteDecomposeResult r =
                KbAgenticAskServiceImpl.parseRewriteJson(raw, "fallback");
        assertEquals("规范问", r.rewritten);
        assertEquals(List.of("子问A", "子问B"), r.subQuestions);
    }

    @Test
    public void mergeQueryRecalls_keepsHighestScorePerChunk() {
        KbAskServiceImpl impl = new KbAskServiceImpl();
        KbChunkAskRow row = new KbChunkAskRow();
        row.setChunkId(100L);
        row.setDocumentId(10L);
        row.setSlug("guides/a");

        KbAskServiceImpl.ChunkScored low = new KbAskServiceImpl.ChunkScored(row, 5);
        KbAskServiceImpl.ChunkScored high = new KbAskServiceImpl.ChunkScored(row, 20);

        KbAskServiceImpl.QueryRecallResult q1 = new KbAskServiceImpl.QueryRecallResult(
                "q1", new KbAskServiceImpl.Scope(), List.of("q1"),
                List.of(low), new ArrayList<>());
        KbAskServiceImpl.QueryRecallResult q2 = new KbAskServiceImpl.QueryRecallResult(
                "q2", new KbAskServiceImpl.Scope(), List.of("q2"),
                List.of(high), new ArrayList<>());

        KbAskServiceImpl.MergedPool pool = impl.mergeQueryRecalls(List.of(q1, q2));
        assertEquals(1, pool.mergedChunks.size());
        assertEquals(20, pool.mergedChunks.get(0).score);
        assertTrue(pool.poolSlugs.contains("guides/a"));
    }

    @Test
    public void filterCitationsToPool_removesOutOfPoolSlugs() {
        AskResponse.Citation in = new AskResponse.Citation();
        in.setSlug("guides/a");
        AskResponse.Citation out = new AskResponse.Citation();
        out.setSlug("guides/other");
        List<AskResponse.Citation> filtered = KbAskServiceImpl.filterCitationsToPool(
                List.of(in, out), java.util.Set.of("guides/a"));
        assertEquals(1, filtered.size());
        assertEquals("guides/a", filtered.get(0).getSlug());
    }

    @Test
    public void parseSelfCheckJson_computesCoverage() {
        String raw = "{\"supported\":[\"A\",\"B\"],\"unsupported\":[\"C\"],\"missingInfo\":[\"关键词\"]}";
        KbAgenticAskServiceImpl.SelfCheckResult r =
                KbAgenticAskServiceImpl.parseSelfCheckJson(raw);
        org.junit.Assert.assertFalse(r.parseFailed);
        org.junit.Assert.assertEquals(2.0 / 3.0, r.coverage, 0.001);
        org.junit.Assert.assertEquals(1, r.missingInfo.size());
    }

    @Test
    public void computeCoverage_emptyStatements_returnsOne() {
        org.junit.Assert.assertEquals(1.0,
                KbAgenticAskServiceImpl.computeCoverage(new ArrayList<>(), new ArrayList<>()), 0.001);
    }

    @Test
    public void parseSelfCheckJson_invalidJson_marksParseFailed() {
        KbAgenticAskServiceImpl.SelfCheckResult r =
                KbAgenticAskServiceImpl.parseSelfCheckJson("not json");
        org.junit.Assert.assertTrue(r.parseFailed);
    }
}
