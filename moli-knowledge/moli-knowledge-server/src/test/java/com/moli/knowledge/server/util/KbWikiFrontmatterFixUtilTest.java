package com.moli.knowledge.server.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class KbWikiFrontmatterFixUtilTest {

    private static final String TODAY = "2026-06-27";

    @Test
    public void fixMissingDates_addsCreatedAndUpdated() {
        String input = "---\ntitle: t\nslug: s\ntype: guide\nstatus: active\n---\n\n# body\n";
        String out = KbWikiFrontmatterFixUtil.fixMissingDates(input, TODAY);
        Assert.assertTrue(out.contains("created: " + TODAY));
        Assert.assertTrue(out.contains("updated: " + TODAY));
    }

    @Test
    public void fixSlugMismatch_alignsFrontmatterSlug() {
        String input = "---\ntitle: t\nslug: wrong\ntype: guide\n---\n\nbody";
        String out = KbWikiFrontmatterFixUtil.fixSlugMismatch(input, "right-name");
        Assert.assertTrue(out.contains("slug: right-name"));
        Assert.assertFalse(out.contains("slug: wrong"));
    }

    @Test
    public void fixMissingSource_infersRawFromBody() {
        String input = "---\ntitle: t\nslug: s\ntype: guide\nstatus: active\n---\n\n见 raw/prd/foo.md\n";
        String out = KbWikiFrontmatterFixUtil.fixMissingSource(input, "guides/s", "wiki");
        Assert.assertTrue(out.contains("sources:"));
        Assert.assertTrue(out.contains("raw/prd/foo.md"));
    }

    @Test
    public void fixContent_appliesMultipleKinds() {
        String input = "---\ntitle: t\nslug: bad\ntype: guide\n---\n\nraw/design/x.md\n";
        Set<String> kinds = new LinkedHashSet<>();
        kinds.add("missing_dates");
        kinds.add("slug_mismatch");
        kinds.add("missing_source");
        String out = KbWikiFrontmatterFixUtil.fixContent(input, "articles/good", "wiki", kinds, TODAY);
        Assert.assertTrue(out.contains("created: " + TODAY));
        Assert.assertTrue(out.contains("slug: good"));
        Assert.assertTrue(out.contains("raw/design/x.md"));
    }

    @Test
    public void stemFromSlug_returnsLastSegment() {
        Assert.assertEquals("foo", KbWikiFrontmatterFixUtil.stemFromSlug("guides/foo"));
    }

    @Test
    public void wikiGovernKindUtil_classifiesKinds() {
        Assert.assertTrue(WikiGovernKindUtil.isScriptFixable("missing_dates"));
        Assert.assertTrue(WikiGovernKindUtil.isAiFixable("broken_link"));
        Assert.assertTrue(WikiGovernKindUtil.isManualOnly("dup_slug"));
        Assert.assertFalse(WikiGovernKindUtil.isAiFixable("missing_dates"));
    }
}
