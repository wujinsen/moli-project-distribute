package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.GraphVo;
import com.moli.knowledge.server.service.KbWikiAiReviseService;
import com.moli.knowledge.server.service.KbWikiEnrichService;
import com.moli.knowledge.server.service.KbWikiFileService;
import com.moli.knowledge.server.service.KbWikiGovernService;
import com.moli.knowledge.server.service.KbWikiGraphService;
import com.moli.knowledge.server.service.KbWikiImportService;
import com.moli.knowledge.server.service.KbWikiLintService;
import com.moli.knowledge.server.testsupport.ControllerTestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbWikiControllerGraphApiTest {

    @InjectMocks
    private KbWikiController controller;

    @Mock
    private KbWikiFileService kbWikiFileService;

    @Mock
    private KbWikiAiReviseService kbWikiAiReviseService;

    @Mock
    private KbWikiEnrichService kbWikiEnrichService;

    @Mock
    private KbWikiLintService kbWikiLintService;

    @Mock
    private KbWikiGovernService kbWikiGovernService;

    @Mock
    private KbWikiImportService kbWikiImportService;

    @Mock
    private KbWikiGraphService kbWikiGraphService;

    @Test
    public void GET_kb_wiki_graph() {
        GraphVo vo = new GraphVo();
        vo.getMeta().setSource("wiki_file");
        vo.getMeta().setMode("summary");
        vo.getMeta().setTotalNodes(12);
        vo.getNodes().add(new GraphVo.Node("guides/a", "Page A", "guide", 3));

        when(kbWikiGraphService.graph(900000000000000001L, "summary", 50, 1)).thenReturn(vo);

        MoliResult<GraphVo> result = controller.wikiGraph(900000000000000001L, "summary", 50, 1);

        ControllerTestSupport.assertSuccess(result);
        Assert.assertNotNull(result.getData());
        Assert.assertEquals("wiki_file", result.getData().getMeta().getSource());
        Assert.assertEquals(1, result.getData().getNodes().size());
        verify(kbWikiGraphService).graph(900000000000000001L, "summary", 50, 1);
    }
}
