package com.moli.knowledge.server.support;

import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.util.KbContentHashUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class KbWikiDriftScannerTest {

    private KbWikiDriftScanner scanner;
    private Path kbRoot;

    @Before
    public void setUp() throws Exception {
        kbRoot = Files.createTempDirectory("kb-drift-scanner-test-");
        Files.createDirectories(kbRoot.resolve("wiki/guides"));

        KbWikiProperties props = new KbWikiProperties();
        props.setRoot(kbRoot.toString());
        Map<String, String> dirs = new LinkedHashMap<>();
        dirs.put("enterprise-kb", "wiki");
        props.setSpaceDirs(dirs);

        scanner = new KbWikiDriftScanner();
        inject(scanner, "wikiProperties", props);
    }

    @Test
    public void scanWikiDir_collectsSlugAndHash() throws Exception {
        String content = "---\ntitle: Page\n---\nhello";
        Path md = kbRoot.resolve("wiki/guides/page.md");
        Files.write(md, content.getBytes(StandardCharsets.UTF_8));

        Map<String, WikiPageSnapshot> wiki = scanner.scanWikiDir("wiki");
        Assert.assertEquals(1, wiki.size());
        WikiPageSnapshot snap = wiki.get("guides/page");
        Assert.assertNotNull(snap);
        Assert.assertEquals("wiki/guides/page.md", snap.getRelativePath());
        Assert.assertEquals(
                KbContentHashUtil.sha256WikiMarkdown(content),
                snap.getContentHash());
    }

    @Test
    public void scanWikiDir_matchesPythonSyncHashForCrlfFile() throws Exception {
        String crlfBytes = "---\r\ntitle: Page\r\n---\r\nhello";
        Path md = kbRoot.resolve("wiki/guides/crlf-page.md");
        Files.write(md, crlfBytes.getBytes(StandardCharsets.UTF_8));

        Map<String, WikiPageSnapshot> wiki = scanner.scanWikiDir("wiki");
        WikiPageSnapshot snap = wiki.get("guides/crlf-page");
        Assert.assertNotNull(snap);
        String lfContent = "---\ntitle: Page\n---\nhello";
        Assert.assertEquals(KbContentHashUtil.sha256(lfContent), snap.getContentHash());
    }

    @Test
    public void scanWikiDir_skipsIndexAndLogMetaPages() throws Exception {
        Files.write(kbRoot.resolve("wiki/index.md"), "# index\n".getBytes(StandardCharsets.UTF_8));
        Files.write(kbRoot.resolve("wiki/log.md"), "# log\n".getBytes(StandardCharsets.UTF_8));
        Files.write(kbRoot.resolve("wiki/guides/ok.md"), "# ok\n".getBytes(StandardCharsets.UTF_8));

        Map<String, WikiPageSnapshot> wiki = scanner.scanWikiDir("wiki");
        Assert.assertFalse(wiki.containsKey("index"));
        Assert.assertFalse(wiki.containsKey("log"));
        Assert.assertTrue(wiki.containsKey("guides/ok"));
    }

    @Test
    public void resolveWikiDirForSpace_returnsConfiguredDir() {
        Assert.assertEquals("wiki", scanner.resolveWikiDirForSpace("enterprise-kb"));
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
