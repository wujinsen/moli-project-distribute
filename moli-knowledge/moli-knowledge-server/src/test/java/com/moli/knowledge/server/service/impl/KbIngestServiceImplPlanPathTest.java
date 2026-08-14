package com.moli.knowledge.server.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbIngestProperties;
import com.moli.knowledge.server.entity.KbCategory;
import com.moli.knowledge.server.mapper.KbCategoryMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbIngestServiceImplPlanPathTest {

    private static final Long SPACE_ID = 1L;
    private static final Long OTHER_SPACE_ID = 2L;

    @InjectMocks
    private KbIngestServiceImpl service;

    @Mock
    private KbIngestProperties ingestProperties;

    @Mock
    private KbCategoryMapper kbCategoryMapper;

    @Before
    public void setUp() {
        when(ingestProperties.isEnabled()).thenReturn(true);
    }

    @Test
    public void resolveCreateRelPath_categoryInSameSpace() throws Exception {
        KbCategory cat = category(100L, SPACE_ID, "fe", "FE 题库");
        when(kbCategoryMapper.selectById(100L)).thenReturn(cat);

        JSONObject item = new JSONObject(true);
        item.put("categoryId", 100L);
        item.put("slug", "fe_kamoku_b_set_sample_qs");

        String rel = invokeResolveCreateRelPath(SPACE_ID, item);
        Assert.assertEquals("fe/fe_kamoku_b_set_sample_qs", rel);
    }

    @Test(expected = BaseException.class)
    public void resolveCreateRelPath_rejectsCrossSpaceCategory() throws Exception {
        KbCategory cat = category(100L, OTHER_SPACE_ID, "fe", "FE 题库");
        when(kbCategoryMapper.selectById(100L)).thenReturn(cat);

        JSONObject item = new JSONObject(true);
        item.put("categoryId", 100L);
        item.put("slug", "foo");

        try {
            invokeResolveCreateRelPath(SPACE_ID, item);
        } catch (java.lang.reflect.InvocationTargetException e) {
            if (e.getCause() instanceof BaseException) {
                throw (BaseException) e.getCause();
            }
            throw e;
        }
    }

    private static KbCategory category(Long id, Long spaceId, String dirSlug, String name) {
        KbCategory cat = new KbCategory();
        cat.setId(id);
        cat.setSpaceId(spaceId);
        cat.setDirSlug(dirSlug);
        cat.setCategoryName(name);
        cat.setIsDelete(0);
        return cat;
    }

    private String invokeResolveCreateRelPath(Long spaceId, JSONObject item) throws Exception {
        Method m = KbIngestServiceImpl.class.getDeclaredMethod("resolveCreateRelPath", Long.class, JSONObject.class);
        m.setAccessible(true);
        return (String) m.invoke(service, spaceId, item);
    }
}
