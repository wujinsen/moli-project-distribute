package com.moli.knowledge.server.controller;

import com.moli.knowledge.server.service.KbAssetService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class KbAssetControllerApiTest {

    @InjectMocks
    private KbAssetController controller;

    @Mock
    private KbAssetService kbAssetService;

    @Test
    public void GET_kb_raw_asset() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.rawAsset("wujinsen_markdown/a.png", 900000000000000001L, response);
        verify(kbAssetService).serveRawAsset(900000000000000001L, "wujinsen_markdown/a.png", response);
    }

    @Test
    public void GET_kb_wiki_asset() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.wikiAsset("java/jvm", "gc.png", 900000000000000001L, response);
        verify(kbAssetService).serveWikiAsset(900000000000000001L, "java/jvm", "gc.png", response);
    }
}
