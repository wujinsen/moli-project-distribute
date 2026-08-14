package com.moli.knowledge.server.support;

import com.moli.knowledge.server.dto.retrieval.VectorSearchHit;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KbHybridRrfSupportTest {

    private static final int RRF_K = 60;

    @Test
    public void annexPenaltyLetsNgramOnlyCorrectPageWinOverDualAnnex() {
        long correctId = 100L;
        long annexId = 200L;

        List<Long> ngramIds = Collections.singletonList(correctId);
        VectorSearchHit annexHit = new VectorSearchHit();
        annexHit.setChunkId(annexId);
        annexHit.setSlug("articles/bigdata/annex-hadoop-dump");
        annexHit.setRank(1);
        VectorSearchHit correctHit = new VectorSearchHit();
        correctHit.setChunkId(correctId);
        correctHit.setSlug("articles/jvm-memory-gc");
        correctHit.setRank(5);

        List<VectorSearchHit> hits = Arrays.asList(annexHit, correctHit);
        Map<Long, Double> fused = KbHybridRrfSupport.rrfFuse(ngramIds, hits, RRF_K);
        Map<Long, String> slugs = KbHybridRrfSupport.slugIndexFromVectorHits(hits);
        slugs.put(correctId, "articles/jvm-memory-gc");

        Map<Long, Double> penalized = KbHybridRrfSupport.applyAnnexFusionPenalty(fused, slugs);
        List<Long> ordered = KbHybridRrfSupport.sortChunkIdsByRrf(penalized);

        assertEquals("annex 降权后，仅 ngram 强命中的正确页应排 annex 之前",
                (Long) correctId, ordered.get(0));
    }

    @Test
    public void rrfFormulaUnchangedForNonAnnex() {
        List<Long> ngramIds = Arrays.asList(1L, 2L);
        VectorSearchHit v = new VectorSearchHit();
        v.setChunkId(3L);
        v.setSlug("guides/foo");
        v.setRank(2);

        Map<Long, Double> fused = KbHybridRrfSupport.rrfFuse(ngramIds, Collections.singletonList(v), RRF_K);
        assertEquals(1.0 / (RRF_K + 1), fused.get(1L), 1e-9);
        assertEquals(1.0 / (RRF_K + 2), fused.get(2L), 1e-9);
        assertEquals(1.0 / (RRF_K + 2), fused.get(3L), 1e-9);
    }

    @Test
    public void isAnnexSlugMatchesBigdataAnnexPath() {
        assertTrue(KbHybridRrfSupport.isAnnexSlug("articles/bigdata/annex-spark-notes"));
    }
}
