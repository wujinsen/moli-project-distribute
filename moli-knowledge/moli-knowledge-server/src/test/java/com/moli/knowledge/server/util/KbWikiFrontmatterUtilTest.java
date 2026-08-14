package com.moli.knowledge.server.util;

import org.junit.Assert;
import org.junit.Test;

public class KbWikiFrontmatterUtilTest {

    private static final String PAGE = "---\n"
            + "title: 测试\n"
            + "slug: wrong-stem\n"
            + "type: guide\n"
            + "created: 2026-01-01\n"
            + "sources:\n"
            + "  - raw/design/x.md\n"
            + "---\n"
            + "## 正文\n";

    @Test
    public void parseSources_blockList() {
        Assert.assertEquals(1, KbWikiFrontmatterUtil.parseSources(PAGE).size());
    }

    @Test
    public void readField_slug() {
        Assert.assertEquals("wrong-stem", KbWikiFrontmatterUtil.readField(PAGE, "slug"));
    }

    @Test
    public void hasH1_falseWhenOnlyH2() {
        Assert.assertFalse(KbWikiFrontmatterUtil.hasH1(PAGE));
    }

    @Test
    public void hasH1_trueWhenH1Present() {
        String md = PAGE.replace("## 正文", "# 标题\n\n正文");
        Assert.assertTrue(KbWikiFrontmatterUtil.hasH1(md));
    }
}
