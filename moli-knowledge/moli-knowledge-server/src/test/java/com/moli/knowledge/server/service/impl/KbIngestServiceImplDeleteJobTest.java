package com.moli.knowledge.server.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbIngestProperties;
import com.moli.knowledge.server.entity.KbIngestJob;
import com.moli.knowledge.server.mapper.KbIngestJobMapper;
import com.moli.knowledge.server.service.KbAclService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbIngestServiceImplDeleteJobTest {

    private static final Long JOB_ID = 900000000000000099L;
    private static final Long SPACE_ID = 900000000000000001L;

    @InjectMocks
    private KbIngestServiceImpl service;

    @Mock
    private KbIngestProperties ingestProperties;

    @Mock
    private KbIngestJobMapper jobMapper;

    @Mock
    private KbAclService kbAclService;

    @Before
    public void setUp() {
        when(ingestProperties.isEnabled()).thenReturn(true);
    }

    @Test(expected = BaseException.class)
    public void deleteJob_rejectsWhenIngestDisabled() {
        when(ingestProperties.isEnabled()).thenReturn(false);
        service.deleteJob(JOB_ID);
    }

    @Test(expected = BaseException.class)
    public void deleteJob_rejectsNullId() {
        service.deleteJob(null);
    }

    @Test(expected = BaseException.class)
    public void deleteJob_rejectsMissingJob() {
        when(jobMapper.selectById(JOB_ID)).thenReturn(null);
        service.deleteJob(JOB_ID);
    }

    @Test(expected = BaseException.class)
    public void deleteJob_rejectsAlreadyDeleted() {
        KbIngestJob job = stubJob();
        job.setIsDelete(1);
        when(jobMapper.selectById(JOB_ID)).thenReturn(job);
        service.deleteJob(JOB_ID);
    }

    @Test(expected = BaseException.class)
    public void deleteJob_propagatesAclDenied() {
        KbIngestJob job = stubJob();
        when(jobMapper.selectById(JOB_ID)).thenReturn(job);
        doThrow(new BaseException("无权编辑该空间")).when(kbAclService).assertCanEdit(SPACE_ID);
        service.deleteJob(JOB_ID);
    }

    @Test
    public void deleteJob_softDeletesWhenEditor() {
        KbIngestJob job = stubJob();
        when(jobMapper.selectById(JOB_ID)).thenReturn(job);

        service.deleteJob(JOB_ID);

        verify(kbAclService).assertCanEdit(eq(SPACE_ID));
        ArgumentCaptor<KbIngestJob> captor = ArgumentCaptor.forClass(KbIngestJob.class);
        verify(jobMapper).updateById(captor.capture());
        Assert.assertEquals(Integer.valueOf(1), captor.getValue().getIsDelete());
    }

    private static KbIngestJob stubJob() {
        KbIngestJob job = new KbIngestJob();
        job.setId(JOB_ID);
        job.setSpaceId(SPACE_ID);
        job.setTopic("test batch");
        job.setStatus("reviewing");
        job.setIsDelete(0);
        return job;
    }
}
