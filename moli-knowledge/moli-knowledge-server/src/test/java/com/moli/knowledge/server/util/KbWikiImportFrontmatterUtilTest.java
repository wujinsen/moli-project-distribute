package com.moli.knowledge.server.util;

import org.junit.Assert;
import org.junit.Test;

public class KbWikiImportFrontmatterUtilTest {

    @Test
    public void prepareImportContent_addsWebImportSource() {
        String raw = "# Title\n\nbody";
        String out = KbWikiImportFrontmatterUtil.prepareImportContent(
                raw, "demo-page", null, "demo.md", null);
        Assert.assertTrue(out.startsWith("---\n"));
        Assert.assertTrue(out.contains("slug: demo-page"));
        Assert.assertTrue(out.contains("web-import:demo.md"));
        Assert.assertTrue(out.contains("# Title"));
        Assert.assertTrue(out.contains("body"));
    }

    @Test
    public void prepareImportContent_preservesCreatedOnOverwrite() {
        String existing = "---\ntitle: Old\ncreated: 2026-01-01\n---\n\n# Old\n";
        String created = KbWikiImportFrontmatterUtil.extractCreated(existing);
        Assert.assertEquals("2026-01-01", created);
        String out = KbWikiImportFrontmatterUtil.prepareImportContent(
                "# New\n", "demo", "New Title", "demo.md", created);
        Assert.assertTrue(out.contains("created: 2026-01-01"));
        Assert.assertTrue(out.contains("title: New Title"));
    }
}
