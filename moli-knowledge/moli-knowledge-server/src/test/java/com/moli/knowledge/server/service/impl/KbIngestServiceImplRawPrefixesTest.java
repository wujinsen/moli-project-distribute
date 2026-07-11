package com.moli.knowledge.server.service.impl;

import com.moli.knowledge.server.config.KbIngestProperties;
import com.moli.knowledge.server.dto.RawPrefixVo;
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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbIngestServiceImplRawPrefixesTest {

    @InjectMocks
    private KbIngestServiceImpl service;

    @Mock
    private KbIngestProperties ingestProperties;

    @Mock
    private KbAclService kbAclService;

    @Mock
    private KbSpaceMapper kbSpaceMapper;

    private Path rawRoot;

    @Before
    public void setUp() throws Exception {
        rawRoot = Files.createTempDirectory("kb-raw-prefixes-test-");
        when(ingestProperties.isEnabled()).thenReturn(true);
        when(ingestProperties.getRawRoot()).thenReturn(rawRoot.toString());

        KbSpace space = new KbSpace();
        space.setId(900000000000000001L);
        space.setSpaceCode("enterprise-kb");
        when(kbSpaceMapper.selectOne(any())).thenReturn(space);
    }

    @Test
    public void rawPrefixes_listsFirstLevelDirectoriesOnly() throws Exception {
        Files.createDirectories(rawRoot.resolve("school/fe"));
        Files.createDirectories(rawRoot.resolve("test-walkthrough"));
        Files.write(rawRoot.resolve("root-file.md"), "# x".getBytes(StandardCharsets.UTF_8));
        Files.createDirectories(rawRoot.resolve(".git"));

        List<RawPrefixVo> prefixes = service.rawPrefixes();
        List<String> names = prefixes.stream().map(RawPrefixVo::getPrefix).collect(Collectors.toList());

        Assert.assertEquals(2, names.size());
        Assert.assertEquals("school", names.get(0));
        Assert.assertEquals("test-walkthrough", names.get(1));
        verify(kbAclService).assertCanRead(900000000000000001L);
    }

    @Test
    public void rawPrefixes_emptyWhenRawRootMissing() {
        when(ingestProperties.getRawRoot()).thenReturn(rawRoot.resolve("missing").toString());

        List<RawPrefixVo> prefixes = service.rawPrefixes();

        Assert.assertTrue(prefixes.isEmpty());
    }
}
