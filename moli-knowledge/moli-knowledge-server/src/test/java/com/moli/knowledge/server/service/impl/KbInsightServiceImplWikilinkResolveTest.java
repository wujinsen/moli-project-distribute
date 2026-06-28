package com.moli.knowledge.server.service.impl;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class KbInsightServiceImplWikilinkResolveTest {

    private static final Long GUIDE_ID = 100L;
    private static final Long SERVICE_ID = 200L;

    @Test
    public void resolve_byTitle() {
        Map<String, Long> titles = index("本地启动指南", GUIDE_ID);
        Map<String, Long> slugs = new HashMap<>();
        Assert.assertEquals(GUIDE_ID, KbInsightServiceImpl.resolveWikilinkTarget("本地启动指南", titles, slugs));
    }

    @Test
    public void resolve_bySlugStem() {
        Map<String, Long> titles = new HashMap<>();
        Map<String, Long> slugs = index("guides/本地启动指南", GUIDE_ID, "本地启动指南", GUIDE_ID);
        Assert.assertEquals(GUIDE_ID, KbInsightServiceImpl.resolveWikilinkTarget("本地启动指南", titles, slugs));
    }

    @Test
    public void resolve_byFullSlug() {
        Map<String, Long> titles = new HashMap<>();
        Map<String, Long> slugs = index("guides/本地启动指南", GUIDE_ID);
        Assert.assertEquals(GUIDE_ID, KbInsightServiceImpl.resolveWikilinkTarget("guides/本地启动指南", titles, slugs));
    }

    @Test
    public void resolve_byServiceStem() {
        Map<String, Long> titles = new HashMap<>();
        Map<String, Long> slugs = index("services/用户中心", SERVICE_ID, "用户中心", SERVICE_ID);
        Assert.assertEquals(SERVICE_ID, KbInsightServiceImpl.resolveWikilinkTarget("用户中心", titles, slugs));
    }

    @Test
    public void resolve_missingReturnsNull() {
        Map<String, Long> titles = index("其它页", GUIDE_ID);
        Map<String, Long> slugs = index("guides/其它页", GUIDE_ID);
        Assert.assertNull(KbInsightServiceImpl.resolveWikilinkTarget("本地启动指南", titles, slugs));
    }

    private static Map<String, Long> index(String key, long id) {
        Map<String, Long> m = new HashMap<>();
        m.put(key, id);
        return m;
    }

    private static Map<String, Long> index(String k1, long id1, String k2, long id2) {
        Map<String, Long> m = new HashMap<>();
        m.put(k1, id1);
        m.put(k2, id2);
        return m;
    }
}
