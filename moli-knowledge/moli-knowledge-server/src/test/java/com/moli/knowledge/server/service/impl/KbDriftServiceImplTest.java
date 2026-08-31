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
    public void drift_detectsWikiOnly() {
        Map<String, WikiPageSnapshot> wiki = new LinkedHashMap<>();
        wiki.put("guides/new-only", new WikiPageSnapshot("guides/new-only", "hash-w", "wiki/guides/new-only.md"));

        when(wikiDriftScanner.scanWikiDir("wiki")).thenReturn(wiki);
        when(kbDocumentMapper.selectList(any())).thenReturn(Collections.emptyList());

        KbDriftReportVo report = service.drift(SPACE_ID, 10);
        Assert.assertTrue(report.isDrifted());
        Assert.assertEquals(1, report.getWikiOnlyCount());
        Assert.assertEquals(0, report.getDbOnlyCount());
    }

    @Test
    public void drift_detectsDbOnly() {
        when(wikiDriftScanner.scanWikiDir("wiki")).thenReturn(Collections.emptyMap());

        KbDocument doc = new KbDocument();
        doc.setSlug("guides/db-only");
        doc.setTitle("DB only");
        doc.setContentHash("hash-d");
        when(kbDocumentMapper.selectList(any())).thenReturn(Collections.singletonList(doc));

        KbDriftReportVo report = service.drift(SPACE_ID, 10);
        Assert.assertEquals(1, report.getDbOnlyCount());
        Assert.assertEquals(1, report.getDbOnly().size());
    }

    @Test
    public void drift_sampleLimitTruncatesDetails() {
        Map<String, WikiPageSnapshot> wiki = new LinkedHashMap<>();
        for (int i = 0; i < 5; i++) {
            wiki.put("guides/w" + i, new WikiPageSnapshot("guides/w" + i, "h" + i, "wiki/guides/w" + i + ".md"));
        }
        when(wikiDriftScanner.scanWikiDir("wiki")).thenReturn(wiki);
        when(kbDocumentMapper.selectList(any())).thenReturn(Collections.emptyList());

        KbDriftReportVo report = service.drift(SPACE_ID, 2);
        Assert.assertEquals(5, report.getWikiOnlyCount());
        Assert.assertEquals(2, report.getWikiOnly().size());
        Assert.assertEquals(2, report.getSampleLimit());
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
        Assert.assertEquals(1, summary.getWikiPageTotal());
        Assert.assertEquals(1, summary.getDbKbPageTotal());
        Assert.assertFalse(summary.isScanEmpty());
    }

    @Test
    public void driftSummary_marksScanEmptyWhenBothSidesZero() {
        KbSpace space = new KbSpace();
        space.setId(SPACE_ID);
        space.setSpaceCode("enterprise-kb");

        when(kbAclService.accessibleSpaceIds()).thenReturn(Collections.singletonList(SPACE_ID));
        when(kbSpaceMapper.selectList(any())).thenReturn(Collections.singletonList(space));
        when(wikiDriftScanner.resolveWikiDirForSpace("enterprise-kb")).thenReturn("wiki");
        when(wikiDriftScanner.scanWikiDir("wiki")).thenReturn(Collections.emptyMap());
        when(kbDocumentMapper.selectList(any())).thenReturn(Collections.emptyList());

        KbOpsDriftSummaryVo summary = service.driftSummary(null, 5);
        Assert.assertFalse(summary.isDrifted());
        Assert.assertTrue(summary.isScanEmpty());
        Assert.assertEquals(1, summary.getSpacesScanned());
        Assert.assertEquals(0, summary.getWikiPageTotal());
        Assert.assertEquals(0, summary.getDbKbPageTotal());
    }
}
