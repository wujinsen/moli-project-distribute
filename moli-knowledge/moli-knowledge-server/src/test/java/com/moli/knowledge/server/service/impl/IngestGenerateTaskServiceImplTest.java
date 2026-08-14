package com.moli.knowledge.server.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbIngestProperties;
import com.moli.knowledge.server.dto.IngestGenerateResultVo;
import com.moli.knowledge.server.dto.IngestGenerateStartVo;
import com.moli.knowledge.server.service.KbIngestService;
import com.moli.knowledge.server.support.IngestGenerateProgressSink;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IngestGenerateTaskServiceImplTest {

    @InjectMocks
    private IngestGenerateTaskServiceImpl service;

    @Mock
    private KbIngestService kbIngestService;

    @Mock
    private KbIngestProperties ingestProperties;

    @Mock(name = "ingestGenerateExecutor")
    private Executor ingestGenerateExecutor;

    @Before
    public void setUp() {
        KbIngestProperties.Generate generate = new KbIngestProperties.Generate();
        generate.setAsyncEnabled(true);
        when(ingestProperties.getGenerate()).thenReturn(generate);
        doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            r.run();
            return null;
        }).when(ingestGenerateExecutor).execute(any(Runnable.class));
    }

    @Test(expected = BaseException.class)
    public void start_rejectsWhenAsyncDisabled() {
        KbIngestProperties.Generate generate = new KbIngestProperties.Generate();
        generate.setAsyncEnabled(false);
        when(ingestProperties.getGenerate()).thenReturn(generate);
        service.start(1L, false, true);
    }

    @Test
    public void start_returnsTaskIdAndRunsGenerate() {
        when(kbIngestService.countGeneratePages(1L)).thenReturn(3);

        IngestGenerateResultVo result = new IngestGenerateResultVo();
        result.setTotal(3);
        result.setGenerated(2);
        result.setSkipped(1);
        result.setFailed(0);
        when(kbIngestService.generateWithProgress(eq(1L), eq(false), eq(true), any()))
                .thenReturn(result);

        IngestGenerateStartVo vo = service.start(1L, false, true);

        Assert.assertNotNull(vo.getTaskId());
        Assert.assertEquals(Long.valueOf(1L), vo.getJobId());
        Assert.assertEquals(3, vo.getTotal());
        Assert.assertEquals("running", vo.getStatus());

        ArgumentCaptor<IngestGenerateProgressSink> sinkCaptor = ArgumentCaptor.forClass(IngestGenerateProgressSink.class);
        verify(kbIngestService).generateWithProgress(eq(1L), eq(false), eq(true), sinkCaptor.capture());
        Assert.assertNotNull(sinkCaptor.getValue());
    }

    @Test
    public void stream_returnsEmitterForValidTask() {
        when(kbIngestService.countGeneratePages(2L)).thenReturn(1);
        IngestGenerateResultVo result = new IngestGenerateResultVo();
        result.setTotal(1);
        result.setGenerated(1);
        when(kbIngestService.generateWithProgress(eq(2L), anyBoolean(), anyBoolean(), any()))
                .thenReturn(result);

        IngestGenerateStartVo started = service.start(2L, false, true);
        SseEmitter emitter = service.stream(2L, started.getTaskId());

        Assert.assertNotNull(emitter);
    }

    @Test(expected = BaseException.class)
    public void stream_rejectsMismatchedJobId() {
        when(kbIngestService.countGeneratePages(2L)).thenReturn(1);
        IngestGenerateResultVo result = new IngestGenerateResultVo();
        result.setTotal(1);
        result.setGenerated(1);
        when(kbIngestService.generateWithProgress(eq(2L), anyBoolean(), anyBoolean(), any()))
                .thenReturn(result);

        IngestGenerateStartVo started = service.start(2L, false, true);
        service.stream(99L, started.getTaskId());
    }
}
