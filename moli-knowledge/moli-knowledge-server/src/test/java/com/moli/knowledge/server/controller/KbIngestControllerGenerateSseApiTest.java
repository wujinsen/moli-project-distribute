package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.IngestGenerateResultVo;
import com.moli.knowledge.server.dto.IngestGenerateStartVo;
import com.moli.knowledge.server.service.IngestGenerateTaskService;
import com.moli.knowledge.server.service.KbIngestService;
import com.moli.knowledge.server.service.KbRawCoverageService;
import com.moli.knowledge.server.service.KbRawUploadService;
import com.moli.knowledge.server.config.KbIngestProperties;
import com.moli.knowledge.server.testsupport.ControllerTestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbIngestControllerGenerateSseApiTest {

    @InjectMocks
    private KbIngestController controller;

    @Mock
    private KbIngestService kbIngestService;

    @Mock
    private IngestGenerateTaskService ingestGenerateTaskService;

    @Mock
    private KbIngestProperties ingestProperties;

    @Mock
    private KbRawCoverageService kbRawCoverageService;

    @Mock
    private KbRawUploadService kbRawUploadService;

    @Test
    public void POST_generate_start() {
        IngestGenerateStartVo vo = new IngestGenerateStartVo();
        vo.setTaskId("task-1");
        vo.setJobId(100L);
        vo.setTotal(5);
        vo.setResume(false);
        vo.setStatus("running");

        when(ingestGenerateTaskService.start(100L, false, true)).thenReturn(vo);

        MoliResult<IngestGenerateStartVo> result = controller.generateStart(100L, false, true);

        ControllerTestSupport.assertSuccess(result);
        Assert.assertEquals("task-1", result.getData().getTaskId());
        verify(ingestGenerateTaskService).start(100L, false, true);
    }

    @Test
    public void POST_generate_sync() {
        IngestGenerateResultVo vo = new IngestGenerateResultVo();
        vo.setTotal(3);
        vo.setGenerated(3);

        when(kbIngestService.generate(100L, true, false)).thenReturn(vo);

        MoliResult<IngestGenerateResultVo> result = controller.generate(100L, true, false);

        ControllerTestSupport.assertSuccess(result);
        Assert.assertEquals(3, result.getData().getGenerated());
        verify(kbIngestService).generate(100L, true, false);
    }

    @Test
    public void GET_generate_stream() {
        SseEmitter emitter = new SseEmitter();
        when(ingestGenerateTaskService.stream(eq(100L), eq("task-1"))).thenReturn(emitter);

        SseEmitter result = controller.generateStream(100L, "task-1");

        Assert.assertSame(emitter, result);
        verify(ingestGenerateTaskService).stream(100L, "task-1");
    }
}
