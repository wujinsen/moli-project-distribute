package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.KbWikiAssetUploadVo;
import com.moli.knowledge.server.service.KbAssetService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    public void POST_kb_wiki_asset() {
        KbWikiAssetUploadVo vo = new KbWikiAssetUploadVo();
        vo.setRel("assets/img-1.png");
        vo.setMarkdown("![a](assets/img-1.png)");
        when(kbAssetService.uploadWikiAsset(eq(900000000000000001L), eq("java/jvm"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(vo);

        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1});
        MoliResult<KbWikiAssetUploadVo> result = controller.uploadWikiAsset("java/jvm", file, 900000000000000001L);

        verify(kbAssetService).uploadWikiAsset(900000000000000001L, "java/jvm", file);
        Assert.assertEquals("assets/img-1.png", result.getData().getRel());
    }
}
