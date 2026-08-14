package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.service.KbIngestService;
import com.moli.knowledge.server.service.KbRawCoverageService;
import com.moli.knowledge.server.testsupport.ControllerTestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class KbIngestControllerDeleteJobApiTest {

    private static final Long JOB_ID = 900000000000000099L;

    @InjectMocks
    private KbIngestController controller;

    @Mock
    private KbIngestService kbIngestService;

    @Mock
    private KbRawCoverageService kbRawCoverageService;

    @Test
    public void DELETE_kb_ingest_jobs_id() {
        MoliResult<Boolean> result = controller.deleteJob(JOB_ID);

        ControllerTestSupport.assertSuccess(result);
        Assert.assertEquals(Boolean.TRUE, result.getData());
        verify(kbIngestService).deleteJob(JOB_ID);
    }
}
