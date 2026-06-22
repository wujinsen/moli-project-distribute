package com.moli.knowledge.server.controller;

import com.moli.common.constant.CommonConstant;
import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.entity.KbAttachment;
import com.moli.knowledge.server.service.KbAttachmentService;
import com.moli.knowledge.server.testsupport.ControllerTestSupport;
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
public class KbAttachmentControllerApiTest {

    @InjectMocks
    private KbAttachmentController controller;

    @Mock
    private KbAttachmentService kbAttachmentService;

    @Test
    public void POST_kb_attachment_upload() {
        KbAttachment attachment = new KbAttachment();
        attachment.setId(1001L);
        attachment.setDocumentId(900L);
        attachment.setFileName("demo.txt");
        attachment.setIsDelete(CommonConstant.UN_DELETE);

        MockMultipartFile file = new MockMultipartFile("file", "demo.txt", "text/plain", new byte[]{1, 2, 3});
        when(kbAttachmentService.upload(900L, file)).thenReturn(attachment);

        MoliResult<KbAttachment> result = controller.upload(900L, file);
        ControllerTestSupport.assertSuccess(result);
    }

    @Test
    public void DELETE_kb_attachment() {
        ControllerTestSupport.assertSuccess(controller.delete(1001L));
        verify(kbAttachmentService).delete(1001L);
    }

    @Test
    public void GET_kb_attachment_download() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        controller.download(1001L, response);
        verify(kbAttachmentService).download(eq(1001L), eq(response));
    }
}
