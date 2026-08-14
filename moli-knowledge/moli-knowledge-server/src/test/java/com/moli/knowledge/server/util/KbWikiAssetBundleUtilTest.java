package com.moli.knowledge.server.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class KbWikiAssetBundleUtilTest {

    @Test
    public void rewriteMarkdownImages_mapsRelativeToAssets() {
        String md = "# T\n\n![](imageFile1.png)\n";
        String out = KbWikiAssetBundleUtil.rewriteMarkdownImages(md, names("imageFile1.png"));
        Assert.assertTrue(out.contains("![](assets/imageFile1.png)"));
    }

    @Test
    public void rewriteMarkdownImages_skipsHttp() {
        String md = "![](https://example.com/a.png)";
        String out = KbWikiAssetBundleUtil.rewriteMarkdownImages(md, names("a.png"));
        Assert.assertEquals(md, out);
    }

    @Test
    public void planFromZip_readsNestedAssetsPath() throws Exception {
        byte[] zip = zipBytes("note_images/imageFile1.png", new byte[]{1, 2, 3});
        KbWikiAssetBundleUtil.AssetBundlePlan plan = KbWikiAssetBundleUtil.planFromZip(
                new ByteArrayInputStream(zip), 1024 * 1024, 10, false);
        Assert.assertTrue(plan.getBaseNames().contains("imageFile1.png"));
        Assert.assertEquals(3, plan.getFilesByBaseName().get("imageFile1.png").length);
    }

    @Test
    public void writeAssetFiles_createsUnderAssetDir() throws Exception {
        Path dir = Files.createTempDirectory("kb-asset-bundle-");
        byte[] zip = zipBytes("a.png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});
        KbWikiAssetBundleUtil.AssetBundlePlan plan = KbWikiAssetBundleUtil.planFromZip(
                new ByteArrayInputStream(zip), 1024 * 1024, 10, false);
        Path assetDir = dir.resolve("ops/page.assets");
        java.util.List<String> imported = KbWikiAssetBundleUtil.writeAssetFiles(assetDir, plan, false, 1024 * 1024);
        Assert.assertEquals(1, imported.size());
        Assert.assertTrue(Files.exists(assetDir.resolve("a.png")));
    }

    private static byte[] zipBytes(String entry, byte[] content) throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos)) {
            zos.putNextEntry(new java.util.zip.ZipEntry(entry));
            zos.write(content);
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    private static Set<String> names(String... items) {
        Set<String> set = new HashSet<>();
        Collections.addAll(set, items);
        return set;
    }
}
