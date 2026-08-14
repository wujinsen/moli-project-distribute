package com.moli.knowledge.server.support;

import org.junit.Assert;
import org.junit.Test;

public class KbSlugResolveSupportTest {

    @Test
    public void bareSlug_stripsCategoryPrefix() {
        Assert.assertEquals("本地启动指南", KbSlugResolveSupport.bareSlug("guides/本地启动指南"));
        Assert.assertEquals("用户中心", KbSlugResolveSupport.bareSlug("用户中心"));
    }
}
