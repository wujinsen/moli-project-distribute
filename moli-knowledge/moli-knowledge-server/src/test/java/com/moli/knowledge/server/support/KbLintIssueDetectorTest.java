package com.moli.knowledge.server.support;

import com.moli.knowledge.server.dto.LintVo;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbRelation;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class KbLintIssueDetectorTest {

    @Test
    public void detectDuplicateSlugs_findsStemCollision() {
        KbDocument a = doc(1L, "guides/foo", "A");
        KbDocument b = doc(2L, "develop/foo", "B");
        List<LintVo.Duplicate> dups = KbLintIssueDetector.detectDuplicateSlugs(Arrays.asList(a, b));
        Assert.assertEquals(1, dups.size());
        Assert.assertEquals("foo", dups.get(0).getStem());
        Assert.assertEquals(2, dups.get(0).getSlugs().size());
    }

    @Test
    public void detectStaleByAge_skipsWhenDisabled() {
        KbDocument old = doc(1L, "guides/old", "Old");
        old.setStatus(1);
        old.setUpdateTime(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(200)));
        Assert.assertTrue(KbLintIssueDetector.detectStaleByAge(Collections.singletonList(old), 0).isEmpty());
    }

    @Test
    public void detectStaleByAge_flagsPublishedOldDoc() {
        KbDocument old = doc(1L, "guides/old", "Old");
        old.setStatus(1);
        old.setUpdateTime(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(200)));
        List<LintVo.Stale> stale = KbLintIssueDetector.detectStaleByAge(Collections.singletonList(old), 180);
        Assert.assertEquals(1, stale.size());
        Assert.assertEquals("1", stale.get(0).getSlug());
    }

    @Test
    public void detectSupersededActive_flagsOldPublishedDoc() {
        KbDocument oldDoc = doc(10L, "develop/old", "Old page");
        oldDoc.setStatus(1);
        KbDocument newDoc = doc(20L, "develop/new", "New page");
        Map<Long, KbDocument> byId = new HashMap<>();
        byId.put(10L, oldDoc);
        byId.put(20L, newDoc);

        KbRelation rel = new KbRelation();
        rel.setRelationType("supersedes");
        rel.setSourceDocId(20L);
        rel.setTargetDocId(10L);

        List<LintVo.Stale> stale = KbLintIssueDetector.detectSupersededActive(Collections.singletonList(rel), byId);
        Assert.assertEquals(1, stale.size());
        Assert.assertTrue(stale.get(0).getReason().contains("supersedes"));
    }

    @Test
    public void detectContentHashDuplicates_groupsSameHash() {
        KbDocument a = doc(1L, "a/page", "A");
        a.setContentHash("abc");
        KbDocument b = doc(2L, "b/page", "B");
        b.setContentHash("abc");
        List<LintVo.Conflict> conflicts = KbLintIssueDetector.detectContentHashDuplicates(Arrays.asList(a, b));
        Assert.assertEquals(1, conflicts.size());
        Assert.assertEquals(2, conflicts.get(0).getSlugs().size());
    }

    @Test
    public void bareSlugStem_extractsTail() {
        Assert.assertEquals("foo", KbLintIssueDetector.bareSlugStem("guides/foo"));
        Assert.assertEquals("bar", KbLintIssueDetector.bareSlugStem("bar"));
    }

    @Test
    public void detectFrontmatterIssues_flagsBadType() {
        KbDocument doc = doc(2L, "guides/bad-type", "Bad");
        doc.setContent("---\ntitle: Bad\ntype: not_a_real_type\nsources:\n - a.md\ncreated: 2026-01-01\nupdated: 2026-01-02\n---\n");
        LintVo vo = new LintVo();
        KbLintIssueDetector.detectFrontmatterIssues(Collections.singletonList(doc), vo);
        Assert.assertEquals(1, vo.getBadTypes().size());
        Assert.assertTrue(vo.getBadTypes().get(0).getDetail().contains("not_a_real_type"));
    }

    @Test
    public void detectFrontmatterIssues_skipsMetaPages() {
        KbDocument index = doc(3L, "index", "Index");
        index.setContent("---\n---\n");
        LintVo vo = new LintVo();
        KbLintIssueDetector.detectFrontmatterIssues(Collections.singletonList(index), vo);
        Assert.assertTrue(vo.getMissingSources().isEmpty());
    }

    @Test
    public void detectFrontmatterIssues_flagsMissingSourceAndSlugMismatch() {
        KbDocument doc = doc(1L, "guides/page", "Page");
        doc.setContent("---\ntitle: Page\nslug: other\n type: guide\nupdated: 2026-01-01\n---\n## body");
        LintVo vo = new LintVo();
        KbLintIssueDetector.detectFrontmatterIssues(java.util.Collections.singletonList(doc), vo);
        Assert.assertEquals(1, vo.getMissingSources().size());
        Assert.assertEquals(1, vo.getSlugMismatches().size());
        Assert.assertEquals(1, vo.getMissingDates().size());
    }

    @Test
    public void detectMissingConcepts_whenEnoughRefs() {
        java.util.Map<String, java.util.Set<String>> broken = new java.util.HashMap<>();
        java.util.Set<String> refs = new java.util.LinkedHashSet<>();
        refs.add("a/s1");
        refs.add("b/s2");
        refs.add("c/s3");
        broken.put("概念X", refs);
        java.util.List<LintVo.IssueItem> items = KbLintIssueDetector.detectMissingConcepts(broken, 3);
        Assert.assertEquals(1, items.size());
    }

    private static KbDocument doc(long id, String slug, String title) {
        KbDocument d = new KbDocument();
        d.setId(id);
        d.setSlug(slug);
        d.setTitle(title);
        return d;
    }
}
