package com.moli.knowledge.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.moli.knowledge.server.config.KbSyncProperties;
import com.moli.knowledge.server.dto.SyncStatusVo;
import com.moli.knowledge.server.dto.SyncTriggerVo;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.entity.KbSyncLog;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.mapper.KbSyncLogMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbSyncAlertService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbSyncServiceImplStatusTest {

    @InjectMocks
    private KbSyncServiceImpl service;

    @Mock
    private KbSyncLogMapper kbSyncLogMapper;
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
    public void status_reports_running_from_redis_lock() {
        KbSpace space = new KbSpace();
        space.setId(1L);
        space.setSpaceCode("enterprise-kb");
        when(kbSpaceMapper.selectById(1L)).thenReturn(space);
        when(syncProperties.isLockEnabled()).thenReturn(true);
        when(stringRedisTemplate.hasKey("kb:sync:lock:enterprise-kb")).thenReturn(true);
        when(kbSyncLogMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        SyncStatusVo vo = service.status(1L);

        Assert.assertTrue(vo.isRunning());
        Assert.assertEquals("enterprise-kb", vo.getSpaceCode());
        Assert.assertEquals("running", vo.getLastStatus());
    }

    @Test
    public void status_reads_batch_summary_and_counts() {
        KbSpace space = new KbSpace();
        space.setId(1L);
        space.setSpaceCode("enterprise-kb");
        when(kbSpaceMapper.selectById(1L)).thenReturn(space);
        when(syncProperties.isLockEnabled()).thenReturn(true);
        when(stringRedisTemplate.hasKey("kb:sync:lock:enterprise-kb")).thenReturn(false);

        KbSyncLog batch = new KbSyncLog();
        batch.setBatchNo("20260711120000");
        batch.setSpaceId(1L);
        batch.setAction("batch");
        batch.setStatus("success");
        batch.setMessage("insert=2 update=1 skip=0 fail=0");
        batch.setCreateTime(new Date());
        when(kbSyncLogMapper.selectOne(any(Wrapper.class))).thenReturn(batch);

        KbSyncLog insert = new KbSyncLog();
        insert.setAction("insert");
        insert.setStatus("success");
        KbSyncLog fail = new KbSyncLog();
        fail.setAction("update");
        fail.setStatus("fail");
        when(kbSyncLogMapper.selectList(any(Wrapper.class)))
                .thenReturn(Arrays.asList(batch, insert, fail));

        SyncStatusVo vo = service.status(1L);

        Assert.assertFalse(vo.isRunning());
        Assert.assertEquals("20260711120000", vo.getLastBatchNo());
        Assert.assertEquals("success", vo.getLastStatus());
        Assert.assertEquals("insert=2 update=1 skip=0 fail=0", vo.getLastMessage());
        Assert.assertEquals(2, vo.getTotal());
        Assert.assertEquals(1, vo.getFailCount());
        Assert.assertEquals(1, vo.getSuccessCount());
    }

    @Test
    public void applyTriggerMeta_parses_batch_and_summary() {
        SyncTriggerVo vo = new SyncTriggerVo();
        vo.setSuccess(true);
        String output = "line1\n同步完成 batch=20260711120000：insert=2 update=1 skip=0 fail=0\n";
        service.applyTriggerMeta(vo, output, 0);
        Assert.assertEquals("20260711120000", vo.getBatchNo());
        Assert.assertEquals("success", vo.getStatus());
        Assert.assertEquals("insert=2 update=1 skip=0 fail=0", vo.getMessage());
    }
}
