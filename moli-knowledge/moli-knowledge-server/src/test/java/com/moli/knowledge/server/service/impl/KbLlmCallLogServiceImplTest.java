package com.moli.knowledge.server.service.impl;

import com.moli.common.core.IdGenerator;
import com.moli.knowledge.server.config.KbLlmProperties;
import com.moli.knowledge.server.entity.KbLlmCallLog;
import com.moli.knowledge.server.mapper.KbLlmCallLogMapper;
import com.moli.knowledge.server.service.KbLlmCallLogService;
import com.moli.knowledge.server.util.ShiroUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbLlmCallLogServiceImplTest {

    @InjectMocks
    private KbLlmCallLogServiceImpl service;

    @Mock
    private KbLlmCallLogMapper kbLlmCallLogMapper;
    @Mock
    private KbLlmProperties kbLlmProperties;

    @Before
    public void setUp() {
        when(kbLlmProperties.isCallLogEnabled()).thenReturn(true);
    }

    @Test
    public void recordSuccess_skipsWhenDisabled() {
        when(kbLlmProperties.isCallLogEnabled()).thenReturn(false);
        service.recordSuccess("ask", 1L, "glm", "glm-4-flash", 120);
        verify(kbLlmCallLogMapper, never()).insert(any());
    }

    @Test
    public void recordSuccess_insertsWhenEnabled() {
        try (MockedStatic<IdGenerator> ids = mockStatic(IdGenerator.class);
             MockedStatic<ShiroUtils> shiro = mockStatic(ShiroUtils.class)) {
            ids.when(IdGenerator::getId).thenReturn(100L);
            shiro.when(ShiroUtils::getUserId).thenReturn(1L);

            service.recordSuccess("ask", 1L, "glm", "glm-4-flash", 150);

            ArgumentCaptor<KbLlmCallLog> captor = ArgumentCaptor.forClass(KbLlmCallLog.class);
            verify(kbLlmCallLogMapper).insert(captor.capture());
            Assert.assertEquals("ask", captor.getValue().getScene());
            Assert.assertEquals("success", captor.getValue().getStatus());
        }
    }

    @Test
    public void recordFail_insertsFailStatus() {
        try (MockedStatic<IdGenerator> ids = mockStatic(IdGenerator.class);
             MockedStatic<ShiroUtils> shiro = mockStatic(ShiroUtils.class)) {
            ids.when(IdGenerator::getId).thenReturn(101L);
            shiro.when(ShiroUtils::getUserId).thenReturn(1L);

            service.recordFail("ingest", 2L, "glm", "glm-4-flash", 80, "timeout");

            ArgumentCaptor<KbLlmCallLog> captor = ArgumentCaptor.forClass(KbLlmCallLog.class);
            verify(kbLlmCallLogMapper).insert(captor.capture());
            Assert.assertEquals("fail", captor.getValue().getStatus());
            Assert.assertEquals("timeout", captor.getValue().getErrorMessage());
        }
    }

    @Test
    public void aggregate_groupsCallsByScene() {
        KbLlmCallLog ask = row("ask", "success", 0);
        KbLlmCallLog ingest = row("ingest", "success", 0);
        when(kbLlmCallLogMapper.selectList(any())).thenReturn(Arrays.asList(ask, ingest));

        KbLlmCallLogService.LlmCallStats stats = service.aggregate(Collections.singletonList(1L), false, 7);
        Assert.assertEquals(2, stats.getTotalCalls());
        Assert.assertEquals(Long.valueOf(1), stats.getCallsByScene().get("ask"));
        Assert.assertEquals(Long.valueOf(1), stats.getCallsByScene().get("ingest"));
    }

    @Test
    public void aggregate_countsSuccessAndFail() {
        KbLlmCallLog ok = row("ask", "success", 0);
        KbLlmCallLog fail = row("ask", "fail", 0);
        when(kbLlmCallLogMapper.selectList(any())).thenReturn(Arrays.asList(ok, fail));

        KbLlmCallLogService.LlmCallStats stats = service.aggregate(Collections.singletonList(1L), true, 7);
        Assert.assertEquals(2, stats.getTotalCalls());
        Assert.assertEquals(1, stats.getSuccessCalls());
        Assert.assertEquals(1, stats.getFailCalls());
        Assert.assertEquals(7, stats.getCallTrend().size());
    }

    private static KbLlmCallLog row(String scene, String status, int daysAgo) {
        KbLlmCallLog row = new KbLlmCallLog();
        row.setScene(scene);
        row.setStatus(status);
        row.setSpaceId(1L);
        row.setCreateTime(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(daysAgo)));
        return row;
    }
}
