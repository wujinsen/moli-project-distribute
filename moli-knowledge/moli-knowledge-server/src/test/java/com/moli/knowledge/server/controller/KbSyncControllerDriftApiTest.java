package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.KbDriftReportVo;
import com.moli.knowledge.server.service.KbDriftService;
import com.moli.knowledge.server.service.KbSyncService;
import com.moli.knowledge.server.testsupport.ControllerTestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbSyncControllerDriftApiTest {

    @InjectMocks
    private KbSyncController controller;

    @Mock
    private KbSyncService kbSyncService;
    @Mock
    private KbDriftService kbDriftService;

    @Test
    public void GET_kb_sync_drift() {
        KbDriftReportVo report = new KbDriftReportVo();
        report.setSpaceId(1L);
        report.setDrifted(true);
        report.setHashMismatchCount(2);
        when(kbDriftService.drift(1L, 10)).thenReturn(report);

        MoliResult<KbDriftReportVo> result = controller.drift(1L, 10);

        ControllerTestSupport.assertSuccess(result);
        Assert.assertTrue(result.getData().isDrifted());
        Assert.assertEquals(2, result.getData().getHashMismatchCount());
        verify(kbDriftService).drift(1L, 10);
    }
}
