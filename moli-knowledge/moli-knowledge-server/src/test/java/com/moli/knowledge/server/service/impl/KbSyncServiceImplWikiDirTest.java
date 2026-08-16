package com.moli.knowledge.server.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbWikiProperties;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbSyncServiceImplWikiDirTest {

    @InjectMocks
    private KbSyncServiceImpl service;

    @Mock
    private KbWikiProperties wikiProperties;

    @Test
    public void resolveWikiDirForSpace_opsManual() {
        stubSpaceDirs();
        Assert.assertEquals("wiki-moli", service.resolveWikiDirForSpace("moli-ops-manual"));
    }

    @Test
    public void resolveWikiDirForSpace_enterpriseKb() {
        stubSpaceDirs();
        Assert.assertEquals("wiki", service.resolveWikiDirForSpace("enterprise-kb"));
    }

    @Test(expected = BaseException.class)
    public void resolveWikiDirForSpace_rejectsUnknownSpace() {
        stubSpaceDirs();
        service.resolveWikiDirForSpace("unknown-space");
    }

    private void stubSpaceDirs() {
        Map<String, String> dirs = new LinkedHashMap<>();
        dirs.put("enterprise-kb", "wiki");
        dirs.put("moli-ops-manual", "wiki-moli");
        when(wikiProperties.getSpaceDirs()).thenReturn(dirs);
    }
}
