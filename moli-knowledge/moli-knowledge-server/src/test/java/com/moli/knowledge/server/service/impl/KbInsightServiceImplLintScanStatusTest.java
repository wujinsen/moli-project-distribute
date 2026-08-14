package com.moli.knowledge.server.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbLintScanProperties;
import com.moli.knowledge.server.dto.LintScanStatusVo;
import com.moli.knowledge.server.entity.KbLintIssue;
import com.moli.knowledge.server.entity.KbSpace;
import com.moli.knowledge.server.mapper.KbLintIssueMapper;
import com.moli.knowledge.server.mapper.KbSpaceMapper;
import com.moli.knowledge.server.service.KbAclService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbInsightServiceImplLintScanStatusTest {

    private static final Long SPACE_ID = 100L;

    @InjectMocks
    private KbInsightServiceImpl service;

    @Mock
    private KbLintIssueMapper kbLintIssueMapper;
    @Mock
    private KbSpaceMapper kbSpaceMapper;
    @Mock
    private KbAclService kbAclService;
    @Mock
    private KbLintScanProperties kbLintScanProperties;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    public void scanStatus_readsScheduleConfigAndRedisLastScan() {
        KbSpace space = new KbSpace();
        space.setId(SPACE_ID);
        space.setSpaceCode("enterprise-kb");
        when(kbSpaceMapper.selectById(SPACE_ID)).thenReturn(space);
        when(kbLintScanProperties.isScheduleEnabled()).thenReturn(true);
        when(kbLintScanProperties.getScheduleCron()).thenReturn("0 0 3 ? * MON");
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("kb:lint:last-scan:100")).thenReturn("1718000000000");
        when(kbLintIssueMapper.selectCount(any())).thenReturn(3);

        LintScanStatusVo vo = service.scanStatus(SPACE_ID);

        Assert.assertEquals(SPACE_ID, vo.getSpaceId());
        Assert.assertEquals("enterprise-kb", vo.getSpaceCode());
        Assert.assertTrue(vo.isScheduleEnabled());
        Assert.assertEquals("0 0 3 ? * MON", vo.getScheduleCron());
        Assert.assertEquals(new Date(1718000000000L), vo.getLastScanTime());
        Assert.assertEquals(3, vo.getOpenIssueCount());
        verify(kbAclService).assertCanRead(SPACE_ID);
    }

    @Test(expected = BaseException.class)
    public void scanStatus_globalRequiresAdmin() {
        when(kbAclService.isAdmin()).thenReturn(false);
        service.scanStatus(null);
    }
}
