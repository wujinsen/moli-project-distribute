package com.moli.knowledge.server.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class KbIngestTemplateWriterTest {

    @Test
    public void buildCreatePage_includesFrontmatterAndBody() {
        String out = KbIngestTemplateWriter.buildCreatePage(
                "foo", "标题", "article",
                Arrays.asList("raw/prd/x.md"), Arrays.asList("bar"),
                "# Hello\n\ncontent", "2026-06-27");
        Assert.assertTrue(out.startsWith("---\n"));
        Assert.assertTrue(out.contains("title: 标题"));
        Assert.assertTrue(out.contains("slug: foo"));
        Assert.assertTrue(out.contains("raw/prd/x.md"));
        Assert.assertTrue(out.contains("# Hello"));
    }

    @Test
    public void extractBodyFromRaw_stripsFrontmatter() {
        String raw = "---\ntitle: x\n---\n\nbody only\n";
        Assert.assertEquals("body only", KbIngestTemplateWriter.extractBodyFromRaw(raw));
    }
}
