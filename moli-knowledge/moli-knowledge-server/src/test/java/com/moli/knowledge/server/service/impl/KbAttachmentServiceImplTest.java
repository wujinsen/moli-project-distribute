package com.moli.knowledge.server.service.impl;

import com.moli.common.constant.CommonConstant;
import com.moli.common.core.IdGenerator;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.MinioProperties;
import com.moli.knowledge.server.entity.KbAttachment;
import com.moli.knowledge.server.entity.KbDocument;
import com.moli.knowledge.server.mapper.KbAttachmentMapper;
import com.moli.knowledge.server.mapper.KbDocumentMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.testsupport.MybatisPlusTestSupport;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbAttachmentServiceImplTest {

    private static final Long DOCUMENT_ID = 900L;
    private static final Long ATTACHMENT_ID = 1001L;

    @InjectMocks
    private KbAttachmentServiceImpl service;

    @Mock
    private KbAttachmentMapper kbAttachmentMapper;

    @Mock
    private KbDocumentMapper kbDocumentMapper;

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioProperties minioProperties;

    @Mock
    private KbAclService kbAclService;

    @BeforeClass
    public static void initMybatisPlus() {
        MybatisPlusTestSupport.initAll();
    }

    @Test
    public void upload_success() throws Exception {
        KbDocument document = activeDocument();
        when(kbDocumentMapper.selectById(DOCUMENT_ID)).thenReturn(document);
        when(minioProperties.getBucket()).thenReturn("moli-knowledge");
        doAnswer(invocation -> null).when(minioClient).putObject(
                eq("moli-knowledge"),
                anyString(),
                any(InputStream.class),
                any(PutObjectOptions.class)
        );
        when(kbAttachmentMapper.insert(any(KbAttachment.class))).thenReturn(1);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "demo.txt",
                "text/plain",
                "hello attachment".getBytes(StandardCharsets.UTF_8)
        );

        try (MockedStatic<IdGenerator> idGenerator = mockStatic(IdGenerator.class)) {
            idGenerator.when(IdGenerator::getId).thenReturn(ATTACHMENT_ID);
            KbAttachment result = service.upload(DOCUMENT_ID, file);

            Assert.assertEquals(ATTACHMENT_ID, result.getId());
            Assert.assertEquals(DOCUMENT_ID, result.getDocumentId());
            Assert.assertEquals("demo.txt", result.getFileName());
            Assert.assertEquals("text/plain", result.getContentType());
            Assert.assertEquals(CommonConstant.UN_DELETE, result.getIsDelete());
            Assert.assertTrue(result.getObjectKey().contains("kb/attachment/" + DOCUMENT_ID + "/" + ATTACHMENT_ID));
        }

        ArgumentCaptor<KbAttachment> captor = ArgumentCaptor.forClass(KbAttachment.class);
        verify(kbAttachmentMapper).insert(captor.capture());
        Assert.assertEquals(ATTACHMENT_ID, captor.getValue().getId());
    }

    @Test(expected = BaseException.class)
    public void upload_rejectsMissingDocumentId() {
        service.upload(null, new MockMultipartFile("file", "a.txt", "text/plain", new byte[]{1}));
    }

    @Test(expected = BaseException.class)
    public void upload_rejectsEmptyFile() {
        service.upload(DOCUMENT_ID, new MockMultipartFile("file", "a.txt", "text/plain", new byte[0]));
    }

    @Test(expected = BaseException.class)
    public void upload_rejectsMissingDocument() {
        when(kbDocumentMapper.selectById(DOCUMENT_ID)).thenReturn(null);
        service.upload(DOCUMENT_ID, new MockMultipartFile("file", "a.txt", "text/plain", new byte[]{1}));
    }

    @Test
    public void download_success() throws Exception {
        KbAttachment attachment = activeAttachment();
        when(kbAttachmentMapper.selectById(ATTACHMENT_ID)).thenReturn(attachment);
        when(minioProperties.getBucket()).thenReturn("moli-knowledge");
        when(minioClient.getObject("moli-knowledge", attachment.getObjectKey()))
                .thenReturn(new ByteArrayInputStream("payload".getBytes(StandardCharsets.UTF_8)));

        MockHttpServletResponse response = new MockHttpServletResponse();
        service.download(ATTACHMENT_ID, response);

        Assert.assertEquals("payload", response.getContentAsString());
        Assert.assertEquals("text/plain", response.getContentType());
        Assert.assertTrue(response.getHeader("Content-Disposition").contains("demo.txt"));
    }

    @Test(expected = BaseException.class)
    public void download_rejectsDeletedAttachment() {
        KbAttachment attachment = activeAttachment();
        attachment.setIsDelete(CommonConstant.IS_DELETE);
        when(kbAttachmentMapper.selectById(ATTACHMENT_ID)).thenReturn(attachment);
        service.download(ATTACHMENT_ID, new MockHttpServletResponse());
    }

    @Test
    public void delete_softDeletes() {
        KbAttachment attachment = activeAttachment();
        when(kbAttachmentMapper.selectById(ATTACHMENT_ID)).thenReturn(attachment);
        when(kbAttachmentMapper.updateById(any(KbAttachment.class))).thenReturn(1);

        service.delete(ATTACHMENT_ID);

        ArgumentCaptor<KbAttachment> captor = ArgumentCaptor.forClass(KbAttachment.class);
        verify(kbAttachmentMapper).updateById(captor.capture());
        Assert.assertEquals(CommonConstant.IS_DELETE, captor.getValue().getIsDelete());
    }

    private KbDocument activeDocument() {
        KbDocument document = new KbDocument();
        document.setId(DOCUMENT_ID);
        document.setIsDelete(CommonConstant.UN_DELETE);
        return document;
    }

    private KbAttachment activeAttachment() {
        KbAttachment attachment = new KbAttachment();
        attachment.setId(ATTACHMENT_ID);
        attachment.setDocumentId(DOCUMENT_ID);
        attachment.setFileName("demo.txt");
        attachment.setObjectKey("kb/attachment/" + DOCUMENT_ID + "/" + ATTACHMENT_ID + "/demo.txt");
        attachment.setFileSize(7L);
        attachment.setContentType("text/plain");
        attachment.setIsDelete(CommonConstant.UN_DELETE);
        return attachment;
    }
}
