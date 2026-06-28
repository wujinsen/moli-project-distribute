package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.WikiSpaceLintRequest;
import com.moli.knowledge.server.dto.WikiSpaceLintVo;
import com.moli.knowledge.server.service.KbWikiAiReviseService;
import com.moli.knowledge.server.service.KbWikiEnrichService;
import com.moli.knowledge.server.service.KbWikiFileService;
import com.moli.knowledge.server.service.KbWikiLintService;
import com.moli.knowledge.server.testsupport.ControllerTestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbWikiControllerLintApiTest {

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
    private com.moli.knowledge.server.service.KbWikiGovernService kbWikiGovernService;

    @Test
    public void POST_kb_wiki_lint_space() {
        WikiSpaceLintVo vo = new WikiSpaceLintVo();
        vo.setSpaceCode("enterprise-kb");
        vo.setWikiDir("wiki");
        vo.setExitCode(1);
        HashMap<String, Object> stats = new HashMap<>();
        stats.put("pages", 10);
        stats.put("errors", 1);
        vo.setStats(stats);
        vo.setIssues(Collections.emptyList());

        when(kbWikiLintService.lintSpace(any(WikiSpaceLintRequest.class))).thenReturn(vo);

        WikiSpaceLintRequest request = new WikiSpaceLintRequest();
        request.setSpaceId(900000000000000001L);
        request.setStrict(false);

        MoliResult<WikiSpaceLintVo> result = controller.lintSpace(request);

        ControllerTestSupport.assertSuccess(result);
        Assert.assertNotNull(result.getData());
        Assert.assertEquals("enterprise-kb", result.getData().getSpaceCode());
        Assert.assertEquals("wiki", result.getData().getWikiDir());
        Assert.assertEquals(Integer.valueOf(1), result.getData().getExitCode());
        verify(kbWikiLintService).lintSpace(request);
    }
}
