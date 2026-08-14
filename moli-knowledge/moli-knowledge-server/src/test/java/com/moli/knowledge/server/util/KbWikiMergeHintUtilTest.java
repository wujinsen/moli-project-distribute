package com.moli.knowledge.server.util;

import com.moli.knowledge.server.dto.WikiGovernMergeHintItemVo;
import com.moli.knowledge.server.dto.WikiLintIssueVo;
import org.junit.Assert;
import org.junit.Test;

public class KbWikiMergeHintUtilTest {

    @Test
    public void buildHint_dupContent_parsesRelatedSlug() {
        WikiLintIssueVo issue = new WikiLintIssueVo();
        issue.setKind("dup_content");
        issue.setPage("guides/a");
        issue.setDetail("正文与 [[guides/b]] 完全相同");

        WikiGovernMergeHintItemVo hint = KbWikiMergeHintUtil.buildHint(issue);
        Assert.assertEquals("guides/a", hint.getCanonicalSlug());
        Assert.assertTrue(hint.getRelatedSlugs().contains("guides/b"));
        Assert.assertTrue(hint.getCursorPrompt().contains("guides/a"));
        Assert.assertFalse(hint.getManualSteps().isEmpty());
    }

    @Test
    public void buildHint_dupSlug_parsesFileList() {
        WikiLintIssueVo issue = new WikiLintIssueVo();
        issue.setKind("dup_slug");
        issue.setDetail("裸名 [[foo]] 对应 2 个文件：guides/foo, articles/foo");

        WikiGovernMergeHintItemVo hint = KbWikiMergeHintUtil.buildHint(issue);
        Assert.assertTrue(hint.getRelatedSlugs().size() >= 2);
        Assert.assertTrue(hint.getCursorPrompt().contains("dup_slug"));
    }
}
