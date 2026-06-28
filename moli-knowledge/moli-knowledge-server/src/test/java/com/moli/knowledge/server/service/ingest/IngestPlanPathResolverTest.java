package com.moli.knowledge.server.service.ingest;

import com.alibaba.fastjson.JSONObject;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.entity.KbCategory;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class IngestPlanPathResolverTest {

    private static KbCategory feCategory() {
        KbCategory cat = new KbCategory();
        cat.setId(100L);
        cat.setSpaceId(1L);
        cat.setCategoryName("FE 题库");
        cat.setDirSlug("fe");
        cat.setDefaultType("interview");
        return cat;
    }

    @Test
    public void resolve_withCategoryId_usesDirSlug() {
        JSONObject item = new JSONObject(true);
        item.put("categoryId", 100L);
        item.put("slug", "fe_kamoku_b_set_sample_qs");

        String rel = IngestPlanPathResolver.resolveCreateRelPath(item, feCategory());
        Assert.assertEquals("fe/fe_kamoku_b_set_sample_qs", rel);
    }

    @Test
    public void resolve_legacyTypeOnly_usesTypeDir() {
        JSONObject item = new JSONObject(true);
        item.put("type", "article");
        item.put("slug", "foo");

        String rel = IngestPlanPathResolver.resolveCreateRelPath(item, null);
        Assert.assertEquals("articles/foo", rel);
    }

    @Test
    public void resolve_slugWithSlash_noDoublePrefix() {
        JSONObject item = new JSONObject(true);
        item.put("type", "article");
        item.put("slug", "articles/foo");

        String rel = IngestPlanPathResolver.resolveCreateRelPath(item, null);
        Assert.assertEquals("articles/foo", rel);
    }

    @Test
    public void resolve_slugWithSlashAndMd_stripsMd() {
        JSONObject item = new JSONObject(true);
        item.put("slug", "guides/本地启动指南.md");

        String rel = IngestPlanPathResolver.resolveCreateRelPath(item, null);
        Assert.assertEquals("guides/本地启动指南", rel);
    }

    @Test(expected = BaseException.class)
    public void resolve_categoryWithSlashInSlug_rejects() {
        JSONObject item = new JSONObject(true);
        item.put("categoryId", 100L);
        item.put("slug", "fe/foo");

        IngestPlanPathResolver.resolveCreateRelPath(item, feCategory());
    }

    @Test(expected = BaseException.class)
    public void resolve_categoryMissingDirSlug_rejects() {
        JSONObject item = new JSONObject(true);
        item.put("categoryId", 100L);
        item.put("slug", "foo");

        KbCategory cat = feCategory();
        cat.setDirSlug("");
        IngestPlanPathResolver.resolveCreateRelPath(item, cat);
    }

    @Test
    public void parseCategoryId_acceptsStringAndNumber() {
        JSONObject num = new JSONObject(true);
        num.put("categoryId", 100L);
        Assert.assertEquals(Long.valueOf(100L), IngestPlanPathResolver.parseCategoryId(num));

        JSONObject str = new JSONObject(true);
        str.put("categoryId", "200");
        Assert.assertEquals(Long.valueOf(200L), IngestPlanPathResolver.parseCategoryId(str));
    }

    @Test
    public void stemFromRawPath_extractsFileStem() {
        Assert.assertEquals("fe_kamoku_b_set_sample_qs",
                IngestPlanPathResolver.stemFromRawPath("fe/fe_kamoku_b_set_sample_qs.md"));
    }

    @Test
    public void inferCategoryFromRawSource_matchesDirSlug() {
        Map<String, KbCategory> map = IngestPlanPathResolver.indexCategoriesByDirSlug(
                java.util.Collections.singletonList(feCategory()));
        KbCategory found = IngestPlanPathResolver.inferCategoryFromRawSource(
                "raw/fe/fe_kamoku_b_set_sample_qs.md", map);
        Assert.assertNotNull(found);
        Assert.assertEquals("fe", found.getDirSlug());
    }

    @Test
    public void inferCategoryFromRawSource_noMatchForRootFile() {
        Map<String, KbCategory> map = IngestPlanPathResolver.indexCategoriesByDirSlug(
                java.util.Collections.singletonList(feCategory()));
        Assert.assertNull(IngestPlanPathResolver.inferCategoryFromRawSource("raw/readme.md", map));
    }
}
