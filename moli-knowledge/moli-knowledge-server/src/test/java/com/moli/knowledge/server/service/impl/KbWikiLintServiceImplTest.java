package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbSyncProperties;
import com.moli.knowledge.server.config.KbWikiProperties;
import com.moli.knowledge.server.dto.WikiSpaceLintRequest;
import com.moli.knowledge.server.dto.WikiSpaceLintVo;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.testsupport.MybatisPlusTestSupport;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbWikiLintServiceImplTest {

    private static final Long SPACE_ID = 900000000000000001L;
    private static final String SPACE_CODE = "enterprise-kb";

    @InjectMocks
    private KbWikiLintServiceImpl service;

    @Mock
    private KbWikiProperties wikiProperties;

    @Mock
    private KbSyncProperties syncProperties;

    @Mock
    private KbSpaceMapper kbSpaceMapper;

    @Mock
    private KbAclService kbAclService;

    @BeforeClass
    public static void initMybatisPlus() {
        MybatisPlusTestSupport.initAll();
    }

    @Before
    public void setUpDefaults() {
        when(syncProperties.getSpaceCode()).thenReturn(SPACE_CODE);
        when(syncProperties.getPython()).thenReturn("python");
        when(wikiProperties.getLintTimeoutSeconds()).thenReturn(120);
        Map<String, String> dirs = new LinkedHashMap<>();
        dirs.put(SPACE_CODE, "wiki");
        when(wikiProperties.getSpaceDirs()).thenReturn(dirs);
    }

    @Test(expected = BaseException.class)
    public void lintSpace_rejectsNullRequest() {
        service.lintSpace(null);
    }

    @Test(expected = BaseException.class)
    public void lintSpace_rejectsMissingSpaceById() {
        when(kbSpaceMapper.selectById(SPACE_ID)).thenReturn(null);
        WikiSpaceLintRequest request = new WikiSpaceLintRequest();
        request.setSpaceId(SPACE_ID);
        service.lintSpace(request);
    }

    @Test(expected = BaseException.class)
    public void lintSpace_rejectsMissingSpaceByCode() {
        when(kbSpaceMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        WikiSpaceLintRequest request = new WikiSpaceLintRequest();
        request.setSpaceCode("unknown-space");
        service.lintSpace(request);
    }

    @Test(expected = BaseException.class)
    public void lintSpace_rejectsUnmappedWikiDir() {
        stubSpace(SPACE_ID, SPACE_CODE);
        when(wikiProperties.getSpaceDirs()).thenReturn(new LinkedHashMap<String, String>());
        WikiSpaceLintRequest request = new WikiSpaceLintRequest();
        request.setSpaceId(SPACE_ID);
        service.lintSpace(request);
    }

    @Test(expected = BaseException.class)
    public void lintSpace_rejectsMissingScript() {
        stubSpace(SPACE_ID, SPACE_CODE);
        when(wikiProperties.getLintScriptPath()).thenReturn("nonexistent/lint.py");
        WikiSpaceLintRequest request = new WikiSpaceLintRequest();
        request.setSpaceId(SPACE_ID);
        service.lintSpace(request);
    }

    @Test(expected = BaseException.class)
    public void lintSpace_propagatesAclDenied() {
        stubSpace(SPACE_ID, SPACE_CODE);
        when(wikiProperties.getLintScriptPath()).thenReturn("moli-knowledge/kb/tools/lint.py");
        doThrow(new BaseException("无权编辑该空间")).when(kbAclService).assertCanEdit(SPACE_ID);
        WikiSpaceLintRequest request = new WikiSpaceLintRequest();
        request.setSpaceId(SPACE_ID);
        service.lintSpace(request);
    }

    @Test
    public void lintSpace_assertsEditorBeforeRunningScript() {
        Path script = findLintScript();
        Assume.assumeNotNull("lint.py not found; skip integration", script);

        stubSpace(SPACE_ID, SPACE_CODE);
        when(wikiProperties.getLintScriptPath()).thenReturn(script.toString());

        WikiSpaceLintRequest request = new WikiSpaceLintRequest();
        request.setSpaceId(SPACE_ID);
        request.setStrict(false);

        WikiSpaceLintVo result = service.lintSpace(request);

        verify(kbAclService).assertCanEdit(eq(SPACE_ID));
        Assert.assertEquals(SPACE_CODE, result.getSpaceCode());
        Assert.assertEquals("wiki", result.getWikiDir());
        Assert.assertNotNull(result.getStats());
        Assert.assertNotNull(result.getIssues());
        Assert.assertTrue(result.getStats().containsKey("pages"));
        Assert.assertNotNull(result.getExitCode());
        Assert.assertNotNull(result.getOutputTail());
    }

    @Test
    public void lintSpace_fixtureWiki_detectsBrokenLink() throws Exception {
        Path script = findLintScript();
        Assume.assumeNotNull("lint.py not found; skip integration", script);

        Path fixtureWiki = createFixtureWiki();
        String fixtureCode = "lint-fixture";

        KbSpace space = stubSpace(SPACE_ID, fixtureCode);
        Map<String, String> dirs = new LinkedHashMap<>();
        dirs.put(fixtureCode, fixtureWiki.toString());
        when(wikiProperties.getSpaceDirs()).thenReturn(dirs);
        when(wikiProperties.getLintScriptPath()).thenReturn(script.toString());

        WikiSpaceLintRequest request = new WikiSpaceLintRequest();
        request.setSpaceId(SPACE_ID);

        WikiSpaceLintVo result = service.lintSpace(request);

        Assert.assertEquals(fixtureCode, result.getSpaceCode());
        Assert.assertEquals(fixtureWiki.toString(), result.getWikiDir());
        Assert.assertTrue(((Number) result.getStats().get("pages")).intValue() >= 2);

        boolean hasBroken = false;
        for (com.moli.knowledge.server.dto.WikiLintIssueVo issue : result.getIssues()) {
            if ("broken_link".equals(issue.getKind())
                    && issue.getPage() != null
                    && issue.getPage().contains("lint-fixture-broken")) {
                hasBroken = true;
                Assert.assertEquals("error", issue.getLevel());
                break;
            }
        }
        Assert.assertTrue("expected broken_link on lint-fixture-broken", hasBroken);
        Assert.assertNotEquals(Integer.valueOf(0), result.getExitCode());
    }

    private KbSpace stubSpace(Long id, String code) {
        KbSpace space = new KbSpace();
        space.setId(id);
        space.setSpaceCode(code);
        when(kbSpaceMapper.selectById(id)).thenReturn(space);
        return space;
    }

    private static Path findLintScript() {
        Path cwd = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path[] candidates = new Path[]{
                cwd.resolve("moli-knowledge/kb/tools/lint.py"),
                cwd.resolve("../kb/tools/lint.py"),
                cwd.resolve("../../moli-knowledge/kb/tools/lint.py"),
                cwd.getParent() != null ? cwd.getParent().resolve("kb/tools/lint.py") : cwd,
        };
        for (Path candidate : candidates) {
            if (candidate != null && Files.isRegularFile(candidate)) {
                return candidate.normalize();
            }
        }
        return null;
    }

    private static Path createFixtureWiki() throws Exception {
        Path root = Files.createTempDirectory("kb-lint-fixture-");
        Path guides = root.resolve("guides");
        Files.createDirectories(guides);

        String ok = "---\n"
                + "title: Lint Fixture OK\n"
                + "slug: lint-fixture-ok\n"
                + "type: guide\n"
                + "status: active\n"
                + "tags: [test]\n"
                + "sources:\n"
                + "  - raw/test-fixture.md\n"
                + "related: []\n"
                + "created: 2026-06-27\n"
                + "updated: 2026-06-27\n"
                + "---\n\n"
                + "# OK\n\n"
                + "Valid page for T16a lint-space test.\n";

        String broken = "---\n"
                + "title: Lint Fixture Broken\n"
                + "slug: lint-fixture-broken\n"
                + "type: guide\n"
                + "status: active\n"
                + "tags: [test]\n"
                + "sources:\n"
                + "  - raw/test-fixture.md\n"
                + "related: []\n"
                + "created: 2026-06-27\n"
                + "updated: 2026-06-27\n"
                + "---\n\n"
                + "# Broken\n\n"
                + "Link to [[nonexistent-slug-t16a-fixture]].\n";

        Files.write(guides.resolve("lint-fixture-ok.md"), ok.getBytes(StandardCharsets.UTF_8));
        Files.write(guides.resolve("lint-fixture-broken.md"), broken.getBytes(StandardCharsets.UTF_8));
        return root;
    }
}
