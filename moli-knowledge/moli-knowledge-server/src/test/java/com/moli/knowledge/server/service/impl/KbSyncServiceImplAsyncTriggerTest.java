package com.moli.knowledge.server.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbSyncProperties;
import com.moli.knowledge.server.dto.SyncTriggerVo;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbSyncServiceImplAsyncTriggerTest {

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
    private KbSyncAlertService kbSyncAlertService;

    @Test
    public void trigger_async_returnsSubmittedWhenIdle() {
        KbSpace space = new KbSpace();
        space.setId(1L);
        space.setSpaceCode("enterprise-kb");
        when(kbSpaceMapper.selectById(1L)).thenReturn(space);
        when(syncProperties.isEnabled()).thenReturn(true);
        when(syncProperties.isLockEnabled()).thenReturn(true);
        when(stringRedisTemplate.hasKey("kb:sync:lock:enterprise-kb")).thenReturn(false);

        SyncTriggerVo vo = service.trigger(1L, null, true);

        Assert.assertTrue(Boolean.TRUE.equals(vo.getAsyncSubmitted()));
        Assert.assertEquals(Long.valueOf(1L), vo.getSpaceId());
        Assert.assertEquals("enterprise-kb", vo.getSpaceCode());
        verify(kbAclService).assertCanSyncTrigger(1L);
    }

    @Test
    public void trigger_async_rejectsWhenAlreadyRunning() {
        KbSpace space = new KbSpace();
        space.setId(1L);
        space.setSpaceCode("enterprise-kb");
        when(kbSpaceMapper.selectById(1L)).thenReturn(space);
        when(syncProperties.isEnabled()).thenReturn(true);
        when(syncProperties.isLockEnabled()).thenReturn(true);
        when(stringRedisTemplate.hasKey("kb:sync:lock:enterprise-kb")).thenReturn(true);

        try {
            service.trigger(1L, null, true);
            Assert.fail("expected BaseException");
        } catch (BaseException e) {
            Assert.assertTrue(e.getMessage().contains("正在同步中"));
        }
    }
}
