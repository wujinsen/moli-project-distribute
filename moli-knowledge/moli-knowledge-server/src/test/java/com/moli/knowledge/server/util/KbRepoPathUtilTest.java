package com.moli.knowledge.server.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class KbRepoPathUtilTest {

    private String previousUserDir;

    @Before
    public void rememberUserDir() {
        previousUserDir = System.getProperty("user.dir");
    }

    @After
    public void restoreUserDir() {
        if (previousUserDir != null) {
            System.setProperty("user.dir", previousUserDir);
        }
    }

    @Test
    public void resolvesSyncScriptFromMonorepoRoot() throws Exception {
        Path repo = Files.createTempDirectory("kb-repo-root-");
        Path script = repo.resolve("moli-knowledge/kb/tools/sync_to_db.py");
        Files.createDirectories(script.getParent());
        Files.writeString(script, "# stub");

        System.setProperty("user.dir", repo.toString());
        Path resolved = KbRepoPathUtil.resolveExisting("../kb/tools/sync_to_db.py", "同步脚本");
        Assert.assertEquals(script.normalize(), resolved);
    }

    @Test
    public void resolvesSyncScriptFromServerModule() throws Exception {
        Path temp = Files.createTempDirectory("kb-server-cwd-");
        Path moduleRoot = temp.resolve("moli-knowledge");
        Path server = moduleRoot.resolve("moli-knowledge-server");
        Files.createDirectories(server);
        Path script = moduleRoot.resolve("kb/tools/sync_to_db.py");
        Files.createDirectories(script.getParent());
        Files.writeString(script, "# stub");

        System.setProperty("user.dir", server.toString());
        Path resolved = KbRepoPathUtil.resolveExisting("../kb/tools/sync_to_db.py", "同步脚本");
        Assert.assertEquals(script.normalize(), resolved);
    }

    @Test
    public void resolvesKbRootFromMonorepoRoot() throws Exception {
        Path repo = Files.createTempDirectory("kb-root-repo-");
        Path kbRoot = repo.resolve("moli-knowledge/kb");
        Files.createDirectories(kbRoot.resolve("wiki"));

        System.setProperty("user.dir", repo.toString());
        Path resolved = KbRepoPathUtil.resolveKbRoot("../kb");
        Assert.assertEquals(kbRoot.normalize(), resolved);
    }

    @Test
    public void resolvesKbRootPrefersMoliKnowledgeWhenStaleRepoKbExists() throws Exception {
        Path repo = Files.createTempDirectory("kb-root-stale-");
        Files.createDirectories(repo.resolve("kb/raw"));
        Path kbRoot = repo.resolve("moli-knowledge/kb");
        Files.createDirectories(kbRoot.resolve("wiki"));

        System.setProperty("user.dir", repo.toString());
        Path resolved = KbRepoPathUtil.resolveKbRoot("../kb");
        Assert.assertEquals(kbRoot.normalize(), resolved);
    }

    @Test
    public void resolvesRawRootFromMonorepoRoot() throws Exception {
        Path repo = Files.createTempDirectory("kb-raw-repo-");
        Path rawRoot = repo.resolve("moli-knowledge/kb/raw");
        Files.createDirectories(rawRoot);

        System.setProperty("user.dir", repo.toString());
        Path resolved = KbRepoPathUtil.resolveRawRoot("moli-knowledge/kb/raw");
        Assert.assertEquals(rawRoot.normalize(), resolved);
    }
}
