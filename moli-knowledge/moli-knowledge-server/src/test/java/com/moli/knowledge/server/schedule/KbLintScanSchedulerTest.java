package com.moli.knowledge.server.schedule;

import com.moli.knowledge.server.config.KbLintScanProperties;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbInsightService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbLintScanSchedulerTest {

    @InjectMocks
    private KbLintScanScheduler scheduler;

    @Mock
    private KbLintScanProperties lintScanProperties;
    @Mock
    private KbInsightService kbInsightService;
    @Mock
    private KbSpaceMapper kbSpaceMapper;

    @Test
    public void scheduledScan_skipsWhenDisabled() {
        when(lintScanProperties.isScheduleEnabled()).thenReturn(false);

        scheduler.scheduledScan();

        verify(kbInsightService, never()).scanScheduled(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    public void scheduledScan_usesConfiguredSpaceIds() {
        when(lintScanProperties.isScheduleEnabled()).thenReturn(true);
        when(lintScanProperties.getScheduleSpaceIds()).thenReturn(Arrays.asList(10L, 20L));

        scheduler.scheduledScan();

        verify(kbInsightService).scanScheduled(10L);
        verify(kbInsightService).scanScheduled(20L);
        verify(kbSpaceMapper, never()).selectList(org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void scheduledScan_scansAllSpacesWhenIdsEmpty() {
        when(lintScanProperties.isScheduleEnabled()).thenReturn(true);
        when(lintScanProperties.getScheduleSpaceIds()).thenReturn(Collections.emptyList());

        KbSpace space = new KbSpace();
        space.setId(99L);
        when(kbSpaceMapper.selectList(org.mockito.ArgumentMatchers.any())).thenReturn(Collections.singletonList(space));

        scheduler.scheduledScan();

        verify(kbInsightService).scanScheduled(99L);
    }
}
