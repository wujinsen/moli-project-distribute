package com.moli.knowledge.server.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbSyncProperties;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbSyncAlertService;
import com.moli.knowledge.server.service.KbAclService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbSyncServiceImplLockTest {

    @InjectMocks
    private KbSyncServiceImpl service;

    @Mock
    private KbSpaceMapper kbSpaceMapper;
    @Mock
    private KbAclService kbAclService;
    @Mock
    private KbSyncProperties syncProperties;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private KbSyncAlertService kbSyncAlertService;

    @Test
    public void trigger_rejectsWhenSpaceSyncAlreadyRunning() {
        KbSpace space = new KbSpace();
        space.setId(1L);
        space.setSpaceCode("enterprise-kb");
        when(kbSpaceMapper.selectById(1L)).thenReturn(space);
        when(syncProperties.isEnabled()).thenReturn(true);
        when(syncProperties.isLockEnabled()).thenReturn(true);
        when(syncProperties.getTimeoutSeconds()).thenReturn(300);
        when(syncProperties.getLockExtraSeconds()).thenReturn(60);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), eq(TimeUnit.SECONDS)))
                .thenReturn(false);

        try {
            service.trigger(1L, null);
            Assert.fail("expected BaseException");
        } catch (BaseException e) {
            Assert.assertTrue(e.getMessage().contains("正在同步中"));
        }

        verify(kbAclService).assertCanSyncTrigger(1L);
        verify(valueOperations, never()).get(anyString());
    }
}
