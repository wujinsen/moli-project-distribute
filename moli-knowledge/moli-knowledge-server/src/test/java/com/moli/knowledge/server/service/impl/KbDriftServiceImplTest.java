package com.moli.knowledge.server.service.impl;

import com.moli.knowledge.server.dto.KbDriftReportVo;
import com.moli.knowledge.server.dto.KbOpsDriftSummaryVo;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.support.KbWikiDriftScanner;
import com.moli.knowledge.server.support.WikiPageSnapshot;
import com.moli.knowledge.server.util.KbContentHashUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbDriftServiceImplTest {

    private static final Long SPACE_ID = 1L;

    @InjectMocks
    private KbDriftServiceImpl service;

    @Mock
    private KbSpaceMapper kbSpaceMapper;
    @Mock
    private KbDocumentMapper kbDocumentMapper;
    @Mock
    private KbAclService kbAclService;
    @Mock
    private KbWikiDriftScanner wikiDriftScanner;

    @Before
    public void setUp() {
        KbSpace space = new KbSpace();
        space.setId(SPACE_ID);
        space.setSpaceCode("enterprise-kb");
        when(kbSpaceMapper.selectById(SPACE_ID)).thenReturn(space);
        when(wikiDriftScanner.resolveWikiDirForSpace("enterprise-kb")).thenReturn("wiki");
    }

    @Test
    public void drift_detectsHashMismatch() {
        String content = "---\ntitle: A\n---\nbody";
        String hash = KbContentHashUtil.sha256(content);
        Map<String, WikiPageSnapshot> wiki = new LinkedHashMap<>();
        wiki.put("guides/a", new WikiPageSnapshot("guides/a", hash, "wiki/guides/a.md"));

        KbDocument doc = new KbDocument();
        doc.setSlug("guides/a");
        doc.setTitle("A");
        doc.setContentHash("old-hash");

        when(wikiDriftScanner.scanWikiDir("wiki")).thenReturn(wiki);
        when(kbDocumentMapper.selectList(any())).thenReturn(Collections.singletonList(doc));

        KbDriftReportVo report = service.drift(SPACE_ID, 10);
        Assert.assertTrue(report.isDrifted());
        Assert.assertEquals(1, report.getHashMismatchCount());
        Assert.assertEquals(0, report.getInSyncCount());
    }

    @Test
    public void driftSummary_aggregatesInSync() {
        String content = "---\ntitle: A\n---\nbody";
        String hash = KbContentHashUtil.sha256(content);
        Map<String, WikiPageSnapshot> wiki = new LinkedHashMap<>();
        wiki.put("guides/a", new WikiPageSnapshot("guides/a", hash, "wiki/guides/a.md"));

        KbDocument doc = new KbDocument();
        doc.setSlug("guides/a");
        doc.setContentHash(hash);

        KbSpace space = new KbSpace();
        space.setId(SPACE_ID);
        space.setSpaceCode("enterprise-kb");

        when(kbSpaceMapper.selectById(SPACE_ID)).thenReturn(space);
        when(wikiDriftScanner.resolveWikiDirForSpace("enterprise-kb")).thenReturn("wiki");
        when(wikiDriftScanner.scanWikiDir("wiki")).thenReturn(wiki);
        when(kbDocumentMapper.selectList(any())).thenReturn(Collections.singletonList(doc));

        KbOpsDriftSummaryVo summary = service.driftSummary(SPACE_ID, 5);
        Assert.assertFalse(summary.isDrifted());
        Assert.assertEquals(1, summary.getInSyncTotal());
    }
}
