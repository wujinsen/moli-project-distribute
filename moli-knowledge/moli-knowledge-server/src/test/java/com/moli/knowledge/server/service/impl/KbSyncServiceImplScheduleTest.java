package com.moli.knowledge.server.service.impl;

import com.moli.knowledge.server.config.KbSyncProperties;
import com.moli.knowledge.server.config.KbWikiProperties;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbSyncServiceImplScheduleTest {

    @InjectMocks
    private KbSyncServiceImpl service;

    @Mock
    private KbSyncProperties syncProperties;
    @Mock
    private KbWikiProperties wikiProperties;

    @Test
    public void resolveScheduleSpaceCodes_usesExplicitConfig() {
        when(syncProperties.getScheduleSpaceCodes()).thenReturn(
                Arrays.asList("enterprise-kb", "moli-ops-manual"));
        List<String> codes = service.resolveScheduleSpaceCodes();
        Assert.assertEquals(Arrays.asList("enterprise-kb", "moli-ops-manual"), codes);
    }

    @Test
    public void resolveScheduleSpaceCodes_fallsBackToWikiSpaceDirs() {
        when(syncProperties.getScheduleSpaceCodes()).thenReturn(Collections.emptyList());
        Map<String, String> dirs = new LinkedHashMap<>();
        dirs.put("enterprise-kb", "wiki");
        dirs.put("moli-ops-manual", "wiki-moli");
        dirs.put("jp-fe-ap-exam", "wiki-jp-exam");
        when(wikiProperties.getSpaceDirs()).thenReturn(dirs);

        List<String> codes = service.resolveScheduleSpaceCodes();
        Assert.assertEquals(3, codes.size());
        Assert.assertTrue(codes.contains("jp-fe-ap-exam"));
    }
}
