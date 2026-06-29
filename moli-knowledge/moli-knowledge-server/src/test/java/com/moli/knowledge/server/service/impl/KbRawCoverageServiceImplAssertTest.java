package com.moli.knowledge.server.service.impl;

import com.moli.knowledge.server.config.KbIngestProperties;
import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.dto.IngestRawConflictVo;
import com.moli.knowledge.server.exception.IngestRawConflictException;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.mapper.KbIngestJobMapper;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.testsupport.MybatisPlusTestSupport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbRawCoverageServiceImplAssertTest {

    private static final Long SPACE_ID = 900000000000000002L;
    private static final String SPACE_CODE = "jp-fe-ap-exam";
    private static final Long JOB_ID = 900000000000000100L;

    @InjectMocks
    private KbRawCoverageServiceImpl service;

    @Mock
    private KbIngestProperties ingestProperties;

    @Mock
    private KbWikiProperties wikiProperties;

    @Mock
    private KbSpaceMapper kbSpaceMapper;

    @Mock
    private KbAclService kbAclService;

    @Mock
    private KbIngestJobMapper jobMapper;

    @BeforeClass
    public static void initMybatisPlus() {
        MybatisPlusTestSupport.initAll();
    }

    @Before
    public void setUp() {
        when(ingestProperties.getCoverageCacheSeconds()).thenReturn(300);
        when(ingestProperties.getMaxCoverageFiles()).thenReturn(10000);
        when(jobMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(Collections.emptyList());
    }

    @Test
    public void assertRawOpenForCommit_throwsStructuredConflict() throws Exception {
        Path kbRoot = Files.createTempDirectory("kb-raw-cov-test-");
        Path wikiRoot = kbRoot.resolve("wiki-jp-exam");
        Path guides = wikiRoot.resolve("guides");
        Files.createDirectories(guides);

        String guideMd = "---\n"
                + "title: 日本語試験知识库说明\n"
                + "slug: 日本語試験知识库说明\n"
                + "type: guide\n"
                + "status: active\n"
                + "tags: [test]\n"
                + "sources:\n"
                + "  - kb/raw/school/fe/\n"
                + "related: []\n"
                + "created: 2026-06-27\n"
                + "updated: 2026-06-27\n"
                + "---\n\n"
                + "# body\n";
        Files.write(guides.resolve("日本語試験知识库说明.md"), guideMd.getBytes(StandardCharsets.UTF_8));

        KbSpace space = new KbSpace();
        space.setId(SPACE_ID);
        space.setSpaceCode(SPACE_CODE);
        when(kbSpaceMapper.selectById(SPACE_ID)).thenReturn(space);

        Map<String, String> dirs = new LinkedHashMap<>();
        dirs.put(SPACE_CODE, wikiRoot.toString());
        when(wikiProperties.getSpaceDirs()).thenReturn(dirs);
        when(wikiProperties.getRoot()).thenReturn(kbRoot.toString());

        try {
            service.assertRawOpenForCommit(
                    SPACE_ID,
                    JOB_ID,
                    new HashSet<>(Collections.singletonList("fe/new-page")),
                    Collections.singletonList("school/fe/fe_kamoku_b_set_sample_qs2.md"));

            Assert.fail("expected IngestRawConflictException");
        } catch (IngestRawConflictException e) {
            Assert.assertTrue(e.getErrorMsg().contains("raw/school/fe/fe_kamoku_b_set_sample_qs2.md"));
            IngestRawConflictVo detail = e.getDetail();
            Assert.assertNotNull(detail);
            Assert.assertEquals(IngestRawConflictVo.ERROR_KIND, detail.getErrorKind());
            Assert.assertEquals(SPACE_ID, detail.getSpaceId());
            Assert.assertEquals(JOB_ID, detail.getJobId());
            Assert.assertEquals(1, detail.getConflicts().size());
            Assert.assertEquals("school/fe/fe_kamoku_b_set_sample_qs2.md", detail.getConflicts().get(0).getPath());
            Assert.assertEquals("cluster", detail.getConflicts().get(0).getCoverage());
            Assert.assertEquals("dir_prefix", detail.getConflicts().get(0).getMatchKind());
            Assert.assertTrue(detail.getConflicts().get(0).getWikiSlugs()
                    .contains("guides/日本語試験知识库说明"));
        }
    }

    @Test
    public void assertRawOpenForCommit_allowsEnrichSameSlug() throws Exception {
        Path kbRoot = Files.createTempDirectory("kb-raw-cov-ok-");
        Path wikiRoot = kbRoot.resolve("wiki");
        Path articles = wikiRoot.resolve("articles");
        Files.createDirectories(articles);

        String articleMd = "---\n"
                + "title: Existing\n"
                + "slug: articles/existing\n"
                + "type: article\n"
                + "status: active\n"
                + "tags: [test]\n"
                + "sources:\n"
                + "  - raw/prd/x.md\n"
                + "related: []\n"
                + "created: 2026-06-27\n"
                + "updated: 2026-06-27\n"
                + "---\n\n"
                + "# body\n";
        Files.write(articles.resolve("existing.md"), articleMd.getBytes(StandardCharsets.UTF_8));

        KbSpace space = new KbSpace();
        space.setId(SPACE_ID);
        space.setSpaceCode("enterprise-kb");
        when(kbSpaceMapper.selectById(SPACE_ID)).thenReturn(space);

        Map<String, String> dirs = new LinkedHashMap<>();
        dirs.put("enterprise-kb", wikiRoot.toString());
        when(wikiProperties.getSpaceDirs()).thenReturn(dirs);
        when(wikiProperties.getRoot()).thenReturn(kbRoot.toString());

        service.assertRawOpenForCommit(
                SPACE_ID,
                JOB_ID,
                new HashSet<>(Collections.singletonList("articles/existing")),
                Arrays.asList("raw/prd/x.md", "prd/x.md"));
    }
}
