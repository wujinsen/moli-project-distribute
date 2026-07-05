package com.moli.knowledge.server.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbIngestProperties;
import com.moli.knowledge.server.config.KbWikiProperties;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

import com.moli.knowledge.server.dto.KbWikiAssetUploadVo;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbAssetServiceImplTest {

    private static final Long SPACE_ID = 900000000000000001L;
    private static final String SPACE_CODE = "enterprise-kb";

    @InjectMocks
    private KbAssetServiceImpl service;

    @Mock
    private KbIngestProperties ingestProperties;

    @Mock
    private KbWikiProperties wikiProperties;

    @Mock
    private KbSpaceMapper kbSpaceMapper;

    @Mock
    private KbAclService kbAclService;

    private Path kbRoot;
    private Path rawRoot;
    private Path wikiDir;

    @Before
    public void setUp() throws Exception {
        kbRoot = Files.createTempDirectory("kb-asset-test-");
        rawRoot = kbRoot.resolve("raw");
        wikiDir = kbRoot.resolve("wiki");
        Files.createDirectories(rawRoot);
        Files.createDirectories(wikiDir);

        KbSpace space = new KbSpace();
        space.setId(SPACE_ID);
        space.setSpaceCode(SPACE_CODE);
        when(kbSpaceMapper.selectById(SPACE_ID)).thenReturn(space);

        when(ingestProperties.isEnabled()).thenReturn(true);
        when(ingestProperties.getRawRoot()).thenReturn(rawRoot.toString());
        when(wikiProperties.isEnabled()).thenReturn(true);
        when(wikiProperties.getRoot()).thenReturn(kbRoot.toString());
        when(wikiProperties.getAssetSubdirSuffix()).thenReturn(".assets");
        when(wikiProperties.isAllowSvg()).thenReturn(false);
        when(wikiProperties.getAssetCacheMaxAgeSeconds()).thenReturn(3600);
        when(wikiProperties.getAssetMaxBytes()).thenReturn(5L * 1024 * 1024);

        Map<String, String> dirs = new LinkedHashMap<>();
        dirs.put(SPACE_CODE, "wiki");
        when(wikiProperties.getSpaceDirs()).thenReturn(dirs);
    }

    @Test
    public void serveRawAsset_success() throws Exception {
        Path imgDir = rawRoot.resolve("wujinsen_markdown/demo.note_images");
        Files.createDirectories(imgDir);
        Path img = imgDir.resolve("imageFile1.png");
        Files.write(img, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});

        MockHttpServletResponse response = new MockHttpServletResponse();
        service.serveRawAsset(SPACE_ID, "wujinsen_markdown/demo.note_images/imageFile1.png", response);

        verify(kbAclService).assertCanRead(SPACE_ID);
        Assert.assertEquals("image/png", response.getContentType());
        Assert.assertTrue(response.getHeader("Content-Disposition").contains("inline"));
        Assert.assertEquals(4, response.getContentAsByteArray().length);
    }

    @Test
    public void serveRawAsset_stripsRawPrefix() throws Exception {
        Path img = rawRoot.resolve("school/fe/a.png");
        Files.createDirectories(img.getParent());
        Files.write(img, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});

        MockHttpServletResponse response = new MockHttpServletResponse();
        service.serveRawAsset(SPACE_ID, "raw/school/fe/a.png", response);

        Assert.assertEquals("image/png", response.getContentType());
    }

    @Test(expected = BaseException.class)
    public void serveRawAsset_rejectsTraversal() {
        service.serveRawAsset(SPACE_ID, "../wiki/secret.png", new MockHttpServletResponse());
    }

    @Test
    public void serveWikiAsset_success() throws Exception {
        Path assetDir = wikiDir.resolve("java/jvm.assets");
        Files.createDirectories(assetDir);
        Path img = assetDir.resolve("gc.png");
        Files.write(img, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});

        MockHttpServletResponse response = new MockHttpServletResponse();
        service.serveWikiAsset(SPACE_ID, "java/jvm", "assets/gc.png", response);

        verify(kbAclService).assertCanRead(SPACE_ID);
        Assert.assertEquals("image/png", response.getContentType());
    }

    @Test(expected = BaseException.class)
    public void serveWikiAsset_rejectsSvgByDefault() throws Exception {
        Path assetDir = wikiDir.resolve("demo.assets");
        Files.createDirectories(assetDir);
        Files.write(assetDir.resolve("x.svg"), "<svg/>".getBytes(StandardCharsets.UTF_8));

        service.serveWikiAsset(SPACE_ID, "demo", "x.svg", new MockHttpServletResponse());
    }

    @Test
    public void normalizeAssetRel_stripsAssetsPrefix() {
        Assert.assertEquals("gc.png", KbAssetServiceImpl.normalizeAssetRel("assets/gc.png"));
    }

    @Test
    public void cleanRawRelative_stripsLeadingRaw() {
        Assert.assertEquals("a/b.png", KbAssetServiceImpl.cleanRawRelative("raw/a/b.png"));
    }

    @Test
    public void uploadWikiAsset_success() throws Exception {
        Path md = wikiDir.resolve("java/jvm.md");
        Files.createDirectories(md.getParent());
        Files.write(md, "# jvm\n".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile multipart = new MockMultipartFile(
                "file",
                "gc-diagram.png",
                "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});

        KbWikiAssetUploadVo vo = service.uploadWikiAsset(SPACE_ID, "java/jvm", multipart);

        verify(kbAclService).assertCanEdit(SPACE_ID);
        Assert.assertTrue(vo.getFileName().startsWith("img-"));
        Assert.assertTrue(vo.getRel().startsWith("assets/img-"));
        Assert.assertTrue(vo.getMarkdown().contains("gc-diagram"));
        Assert.assertTrue(vo.getMarkdown().contains(vo.getRel()));
        Path saved = wikiDir.resolve("java/jvm.assets").resolve(vo.getFileName());
        Assert.assertTrue(Files.isRegularFile(saved));
    }

    @Test(expected = BaseException.class)
    public void uploadWikiAsset_requiresExistingWikiFile() {
        MockMultipartFile multipart = new MockMultipartFile(
                "file", "a.png", "image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});
        service.uploadWikiAsset(SPACE_ID, "missing/page", multipart);
    }
}
