package com.moli.knowledge.server.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.dto.GraphVo;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbAclService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbWikiGraphServiceImplTest {

    private static final Long SPACE_ID = 900000000000000001L;
    private static final String SPACE_CODE = "graph-fixture";

    @InjectMocks
    private KbWikiGraphServiceImpl service;

    @Mock
    private KbWikiProperties wikiProperties;

    @Mock
    private KbSpaceMapper kbSpaceMapper;

    @Mock
    private KbAclService kbAclService;

    @Before
    public void setUp() {
        when(wikiProperties.getRoot()).thenReturn(".");
    }

    @Test(expected = BaseException.class)
    public void graph_rejectsNullSpaceId() {
        service.graph(null, "full", null, null);
    }

    @Test
    public void graph_buildsWikilinkRelatedAndEdgesJsonl() throws Exception {
        Path kbRoot = Files.createTempDirectory("kb-graph-test");
        Path wikiDir = kbRoot.resolve("wiki-fixture");
        Files.createDirectories(wikiDir.resolve("graph"));

        writePage(wikiDir, "guides/a.md",
                "---\n"
                + "title: Page A\n"
                + "slug: guides/a\n"
                + "type: guide\n"
                + "related: [guides/b]\n"
                + "---\n"
                + "# Page A\n"
                + "See [[guides/b]] for more.\n");
        writePage(wikiDir, "guides/b.md",
                "---\n"
                + "title: Page B\n"
                + "slug: guides/b\n"
                + "type: guide\n"
                + "---\n"
                + "# Page B\n");
        Files.write(
                wikiDir.resolve("graph/edges.jsonl"),
                "{\"from\":\"guides/a\",\"to\":\"guides/b\",\"type\":\"depends_on\"}\n".getBytes(StandardCharsets.UTF_8));

        stubSpace(SPACE_ID, SPACE_CODE);
        Map<String, String> dirs = new LinkedHashMap<String, String>();
        dirs.put(SPACE_CODE, wikiDir.toString());
        when(wikiProperties.getSpaceDirs()).thenReturn(dirs);

        GraphVo vo = service.graph(SPACE_ID, "full", 50, 0);

        verify(kbAclService).assertCanRead(SPACE_ID);
        Assert.assertEquals("wiki_file", vo.getMeta().getSource());
        Assert.assertEquals(2, vo.getMeta().getTotalNodes());
        Assert.assertEquals(2, vo.getNodes().size());
        Assert.assertTrue(vo.getLinks().stream().anyMatch(l ->
                "guides/a".equals(l.getSource()) && "guides/b".equals(l.getTarget())));
        Assert.assertTrue(vo.getLinks().stream().anyMatch(l -> "depends_on".equals(l.getType())));
    }

    @Test
    public void graph_summaryMode_truncatesToTopNodes() throws Exception {
        Path kbRoot = Files.createTempDirectory("kb-graph-summary");
        Path wikiDir = kbRoot.resolve("wiki-summary");
        Files.createDirectories(wikiDir);

        for (int i = 0; i < 5; i++) {
            writePage(wikiDir, "p" + i + ".md",
                    "---\n"
                    + "title: P" + i + "\n"
                    + "slug: p" + i + "\n"
                    + "type: article\n"
                    + "---\n"
                    + "# P" + i + "\n"
                    + "[[p0]]\n");
        }

        stubSpace(SPACE_ID, SPACE_CODE);
        Map<String, String> dirs = new LinkedHashMap<String, String>();
        dirs.put(SPACE_CODE, wikiDir.toString());
        when(wikiProperties.getSpaceDirs()).thenReturn(dirs);

        GraphVo vo = service.graph(SPACE_ID, "summary", 2, 0);

        Assert.assertEquals("summary", vo.getMeta().getMode());
        Assert.assertEquals(2, vo.getNodes().size());
        Assert.assertTrue(vo.getMeta().isTruncated());
    }

    private void stubSpace(Long id, String code) {
        KbSpace space = new KbSpace();
        space.setId(id);
        space.setSpaceCode(code);
        when(kbSpaceMapper.selectById(id)).thenReturn(space);
    }

    private static void writePage(Path wikiDir, String relative, String content) throws Exception {
        Path file = wikiDir.resolve(relative);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }
}
