package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.dto.SyncTriggerVo;
import com.moli.knowledge.server.dto.WikiImportResultVo;
import com.moli.knowledge.server.dto.WikiLintPreviewVo;
import com.moli.knowledge.server.dto.WikiPageVo;
import com.moli.knowledge.server.entity.KbCategory;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.mapper.KbCategoryMapper;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbSyncService;
import com.moli.knowledge.server.service.KbWikiAiReviseService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * KbWikiImportService 集成测：真实 {@link KbWikiFileServiceImpl} 写临时磁盘，其余依赖 Mock。
 */
@RunWith(MockitoJUnitRunner.class)
public class KbWikiImportServiceImplTest {

    private static final Long SPACE_ID = 900000000000000003L;
    private static final Long CATEGORY_ID = 200L;
    private static final String SPACE_CODE = "moli-ops-manual";

    private KbWikiImportServiceImpl service;
    private KbWikiFileServiceImpl wikiFileService;

    @Mock
    private KbWikiProperties wikiProperties;
    @Mock
    private KbSpaceMapper kbSpaceMapper;
    @Mock
    private KbCategoryMapper kbCategoryMapper;
    @Mock
    private KbDocumentMapper kbDocumentMapper;
    @Mock
    private KbAclService kbAclService;
    @Mock
    private KbWikiAiReviseService kbWikiAiReviseService;
    @Mock
    private KbSyncService kbSyncService;

    private Path kbRoot;

    @Before
    public void setUp() throws Exception {
        kbRoot = Files.createTempDirectory("kb-wiki-import-test-");
        Files.createDirectories(kbRoot.resolve("wiki-moli/ops"));

        KbSpace space = new KbSpace();
        space.setId(SPACE_ID);
        space.setSpaceCode(SPACE_CODE);
        when(kbSpaceMapper.selectById(SPACE_ID)).thenReturn(space);

        KbCategory category = new KbCategory();
        category.setId(CATEGORY_ID);
        category.setSpaceId(SPACE_ID);
        category.setDirSlug("ops");
        category.setCategoryName("运维");
        category.setIsDelete(0);
        when(kbCategoryMapper.selectById(CATEGORY_ID)).thenReturn(category);

        when(wikiProperties.isEnabled()).thenReturn(true);
        when(wikiProperties.getRoot()).thenReturn(kbRoot.toString());
        when(wikiProperties.getMaxBytes()).thenReturn(2L * 1024 * 1024);
        when(wikiProperties.getAssetSubdirSuffix()).thenReturn(".assets");
        when(wikiProperties.getAssetMaxBytes()).thenReturn(5L * 1024 * 1024);
        when(wikiProperties.getImportAssetsZipMaxBytes()).thenReturn(50L * 1024 * 1024);
        when(wikiProperties.getImportAssetsMaxEntries()).thenReturn(200);
        when(wikiProperties.isAllowSvg()).thenReturn(false);
        Map<String, String> dirs = new LinkedHashMap<>();
        dirs.put(SPACE_CODE, "wiki-moli");
        when(wikiProperties.getSpaceDirs()).thenReturn(dirs);

        wikiFileService = new KbWikiFileServiceImpl();
        inject(wikiFileService, "wikiProperties", wikiProperties);
        inject(wikiFileService, "kbSpaceMapper", kbSpaceMapper);
        inject(wikiFileService, "kbAclService", kbAclService);

        service = new KbWikiImportServiceImpl();
        inject(service, "wikiProperties", wikiProperties);
        inject(service, "kbSpaceMapper", kbSpaceMapper);
        inject(service, "kbCategoryMapper", kbCategoryMapper);
        inject(service, "kbDocumentMapper", kbDocumentMapper);
        inject(service, "kbAclService", kbAclService);
        inject(service, "kbWikiFileService", wikiFileService);
        inject(service, "kbWikiAiReviseService", kbWikiAiReviseService);
        inject(service, "kbSyncService", kbSyncService);
    }

    @Test
    public void importPage_writesWikiFileAndTriggersSync() throws Exception {
        SyncTriggerVo trigger = new SyncTriggerVo();
        trigger.setSuccess(true);
        trigger.setSpaceId(SPACE_ID);
        when(kbSyncService.triggerAfterEdit(SPACE_ID)).thenReturn(trigger);

        KbDocument doc = new KbDocument();
        doc.setId(900123L);
        when(kbDocumentMapper.selectOne(ArgumentMatchers.any(LambdaQueryWrapper.class))).thenReturn(doc);

        MockMultipartFile file = new MockMultipartFile(
                "file", "ops-manual.md", "text/plain",
                "# Ops Manual\n\nbody".getBytes(StandardCharsets.UTF_8));

        WikiImportResultVo result = service.importPage(
                SPACE_ID, CATEGORY_ID, file, "ops-manual", "Ops Manual", "FAIL", false, true, null);

        Assert.assertEquals("ops/ops-manual", result.getSlug());
        Assert.assertTrue(result.isCreated());
        Assert.assertEquals("wiki-moli/ops/ops-manual.md", result.getRelativePath());
        Assert.assertTrue(result.getSync().isTriggered());
        Assert.assertTrue(result.getSync().isSuccess());
        Assert.assertEquals(Long.valueOf(900123L), result.getSync().getDocumentId());
        Assert.assertFalse(result.getNextSteps().isEmpty());

        Path written = kbRoot.resolve("wiki-moli/ops/ops-manual.md");
        Assert.assertTrue(Files.exists(written));
        String disk = new String(Files.readAllBytes(written), StandardCharsets.UTF_8);
        Assert.assertTrue(disk.contains("web-import:ops-manual.md"));
        Assert.assertTrue(disk.contains("slug: ops-manual"));
        Assert.assertTrue(disk.contains("body"));

        verify(kbAclService, atLeastOnce()).assertCanEdit(SPACE_ID);
        verify(kbSyncService).triggerAfterEdit(SPACE_ID);
    }

    @Test
    public void importPage_skipsSyncWhenDisabled() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "demo.md", "text/plain", "# Demo\n".getBytes(StandardCharsets.UTF_8));

        WikiImportResultVo result = service.importPage(
                SPACE_ID, CATEGORY_ID, file, null, null, "FAIL", false, false, null);

        Assert.assertFalse(result.getSync().isTriggered());
        Assert.assertEquals("未触发 Sync", result.getSync().getMessage());
        Assert.assertFalse(result.getNextSteps().isEmpty());
    }

    @Test(expected = BaseException.class)
    public void importPage_failWhenSlugExists() throws Exception {
        Path existing = kbRoot.resolve("wiki-moli/ops/exists.md");
        Files.write(existing, "---\ntitle: old\n---\n".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile file = new MockMultipartFile(
                "file", "exists.md", "text/plain", "# New\n".getBytes(StandardCharsets.UTF_8));
        service.importPage(SPACE_ID, CATEGORY_ID, file, null, null, "FAIL", false, false, null);
    }

    @Test
    public void importPage_withAssetsZip_rewritesImagesAndWritesAssets() throws Exception {
        byte[] png = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};
        MockMultipartFile assetsZip = new MockMultipartFile(
                "assetsZip", "assets.zip", "application/zip", zipEntry("imageFile1.png", png));

        MockMultipartFile file = new MockMultipartFile(
                "file", "with-image.md", "text/plain",
                "# Doc\n\n![](imageFile1.png)\n".getBytes(StandardCharsets.UTF_8));

        WikiImportResultVo result = service.importPage(
                SPACE_ID, CATEGORY_ID, file, "with-image", null, "FAIL", false, false, assetsZip);

        Assert.assertEquals(1, result.getAssetsImported().size());
        Assert.assertEquals("assets/imageFile1.png", result.getAssetsImported().get(0));

        Path md = kbRoot.resolve("wiki-moli/ops/with-image.md");
        String disk = new String(Files.readAllBytes(md), StandardCharsets.UTF_8);
        Assert.assertTrue(disk.contains("![](assets/imageFile1.png)"));

        Path img = kbRoot.resolve("wiki-moli/ops/with-image.assets/imageFile1.png");
        Assert.assertTrue(Files.exists(img));
        Assert.assertEquals(4, Files.size(img));
    }

    private static byte[] zipEntry(String name, byte[] content) throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos)) {
            zos.putNextEntry(new java.util.zip.ZipEntry(name));
            zos.write(content);
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    @Test
    public void importPage_overwriteExisting() throws Exception {
        Path existing = kbRoot.resolve("wiki-moli/ops/overwrite.md");
        Files.write(existing, ("---\ntitle: old\ncreated: 2026-01-01\n---\n\n# Old\n")
                .getBytes(StandardCharsets.UTF_8));

        MockMultipartFile file = new MockMultipartFile(
                "file", "overwrite.md", "text/plain", "# New body\n".getBytes(StandardCharsets.UTF_8));

        WikiImportResultVo result = service.importPage(
                SPACE_ID, CATEGORY_ID, file, null, "New Title", "OVERWRITE", false, false, null);

        Assert.assertFalse(result.isCreated());
        String disk = new String(Files.readAllBytes(existing), StandardCharsets.UTF_8);
        Assert.assertTrue(disk.contains("created: 2026-01-01"));
        Assert.assertTrue(disk.contains("New body"));
    }

    @Test
    public void importPage_lintPreviewCollectsWarnings() {
        WikiLintPreviewVo lintVo = new WikiLintPreviewVo();
        lintVo.setIssues(Collections.singletonList(
                new WikiLintPreviewVo.Item("broken_link", "链接目标不存在")));
        when(kbWikiAiReviseService.previewLint(ArgumentMatchers.any())).thenReturn(lintVo);

        MockMultipartFile file = new MockMultipartFile(
                "file", "lint.md", "text/plain", "# Lint\n".getBytes(StandardCharsets.UTF_8));

        WikiImportResultVo result = service.importPage(
                SPACE_ID, CATEGORY_ID, file, null, null, "FAIL", true, false, null);
        Assert.assertTrue(result.getLintWarnings().get(0).contains("broken_link"));
    }

    @Test(expected = BaseException.class)
    public void importPage_rejectsNonMd() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "x".getBytes(StandardCharsets.UTF_8));
        service.importPage(SPACE_ID, CATEGORY_ID, file, null, null, "FAIL", false, false, null);
    }

    private static void inject(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
