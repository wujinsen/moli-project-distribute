package com.moli.knowledge.server.support;

import com.moli.knowledge.server.dto.LintIssueTypeVo;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class KbLintIssueTypesTest {

    @Test
    public void all_containsTwelveWebIssueTypes() {
        Assert.assertEquals(12, KbLintIssueTypes.all().size());
        Assert.assertTrue(KbLintIssueTypes.all().contains(KbLintIssueTypes.BROKEN_LINK));
        Assert.assertTrue(KbLintIssueTypes.all().contains(KbLintIssueTypes.NO_SUMMARY));
    }

    @Test
    public void descriptors_mapsDuplicateToDupSlug() {
        LintIssueTypeVo duplicate = findByCode("duplicate");
        Assert.assertNotNull(duplicate);
        Assert.assertEquals("dup_slug", duplicate.getLintPyKind());
        Assert.assertFalse(duplicate.isWebOnly());
        Assert.assertFalse(duplicate.isLintPyOnly());
    }

    @Test
    public void descriptors_marksNoSummaryWebOnly() {
        LintIssueTypeVo noSummary = findByCode("no_summary");
        Assert.assertNotNull(noSummary);
        Assert.assertTrue(noSummary.isWebOnly());
        Assert.assertNull(noSummary.getLintPyKind());
    }

    @Test
    public void descriptors_includesLintPyOnlyKinds() {
        List<LintIssueTypeVo> descriptors = KbLintIssueTypes.descriptors();
        Assert.assertTrue(descriptors.stream().anyMatch(t ->
                t.isLintPyOnly() && "space_branding".equals(t.getLintPyKind())));
        Assert.assertTrue(descriptors.stream().anyMatch(t ->
                t.isLintPyOnly() && "near_dup".equals(t.getLintPyKind())));
    }

    @Test
    public void descriptors_staleMapsToOutdated() {
        LintIssueTypeVo stale = findByCode("stale");
        Assert.assertNotNull(stale);
        Assert.assertEquals("outdated", stale.getLintPyKind());
    }

    private static LintIssueTypeVo findByCode(String code) {
        Optional<LintIssueTypeVo> found = KbLintIssueTypes.descriptors().stream()
                .filter(t -> code.equals(t.getCode()))
                .findFirst();
        return found.orElse(null);
    }
}
