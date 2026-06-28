package com.moli.knowledge.server.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class KbWikiFrontmatterUtilTest {

    @Test
    public void parseSources_blockList() {
        String md = "---\ntitle: t\nsources:\n"
                + "  - raw/wujinsen_markdown/a.note.md\n"
                + "  - docs/foo.md\n"
                + "related: []\n---\n\n# body";
        List<String> sources = KbWikiFrontmatterUtil.parseSources(md);
        Assert.assertEquals(2, sources.size());
        Assert.assertEquals("raw/wujinsen_markdown/a.note.md", sources.get(0));
    }

    @Test
    public void normalizeRawSourcePath_variants() {
        Assert.assertEquals("prd/x.md", KbWikiFrontmatterUtil.normalizeRawSourcePath("raw/prd/x.md"));
        Assert.assertEquals("wujinsen_markdown", KbWikiFrontmatterUtil.normalizeRawSourcePath("kb/raw/wujinsen_markdown/"));
        Assert.assertNull(KbWikiFrontmatterUtil.normalizeRawSourcePath("moli-user-center/README.md"));
        Assert.assertNull(KbWikiFrontmatterUtil.normalizeRawSourcePath("https://example.com/a.md"));
    }

    @Test
    public void isDirectoryLikeRawPath() {
        Assert.assertTrue(KbWikiFrontmatterUtil.isDirectoryLikeRawPath("wujinsen_markdown"));
        Assert.assertTrue(KbWikiFrontmatterUtil.isDirectoryLikeRawPath("wujinsen_markdown/foo"));
        Assert.assertFalse(KbWikiFrontmatterUtil.isDirectoryLikeRawPath("wujinsen_markdown/a.note.md"));
    }
}
