package com.moli.knowledge.server.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbIngestProperties;
import com.moli.knowledge.server.dto.RawUploadResultVo;
import com.moli.knowledge.server.service.KbAclService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbRawUploadServiceImplTest {

    private static final Long SPACE_ID = 900000000000000001L;

    @InjectMocks
    private KbRawUploadServiceImpl service;

    @Mock
    private KbIngestProperties ingestProperties;

    @Mock
    private KbAclService kbAclService;

    private Path rawRoot;

    @Before
    public void setUp() throws Exception {
        rawRoot = Files.createTempDirectory("kb-raw-upload-test-");
        when(ingestProperties.isEnabled()).thenReturn(true);
        when(ingestProperties.getRawRoot()).thenReturn(rawRoot.toString());
        when(ingestProperties.getRawUploadMaxBytes()).thenReturn(1024L * 1024);
        when(ingestProperties.getRawUploadMaxFiles()).thenReturn(20);
    }

    @Test
    public void upload_writesMdUnderPrefix() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "demo.md", "text/plain", "# hello".getBytes(StandardCharsets.UTF_8));
        RawUploadResultVo result = service.upload(SPACE_ID, "test-walkthrough", java.util.Collections.singletonList(file), "SKIP");
        Assert.assertEquals(1, result.getUploaded().size());
        Assert.assertEquals("test-walkthrough/demo.md", result.getUploaded().get(0).getPath());
        verify(kbAclService).assertCanRawUpload(SPACE_ID);
    }

    @Test
    public void upload_skipExisting() throws Exception {
        Path target = rawRoot.resolve("test-walkthrough/exists.md");
        Files.createDirectories(target.getParent());
        Files.write(target, "old".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile file = new MockMultipartFile(
                "file", "exists.md", "text/plain", "new".getBytes(StandardCharsets.UTF_8));
        RawUploadResultVo result = service.upload(SPACE_ID, "test-walkthrough", java.util.Collections.singletonList(file), "SKIP");
        Assert.assertEquals(1, result.getSkipped().size());
        Assert.assertEquals("ALREADY_EXISTS", result.getSkipped().get(0).getReason());
        Assert.assertEquals("old", new String(Files.readAllBytes(target), StandardCharsets.UTF_8));
    }

    @Test(expected = BaseException.class)
    public void upload_rejectsTraversalPrefix() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "demo.md", "text/plain", "x".getBytes(StandardCharsets.UTF_8));
        service.upload(SPACE_ID, "../wiki", java.util.Collections.singletonList(file), "SKIP");
    }
}
