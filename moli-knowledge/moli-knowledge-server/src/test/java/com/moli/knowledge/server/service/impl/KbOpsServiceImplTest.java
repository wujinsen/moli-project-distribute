package com.moli.knowledge.server.service.impl;

import com.moli.knowledge.server.dto.KbLlmConfigVo;
import com.moli.knowledge.server.dto.KbOpsDashboardVo;
import com.moli.knowledge.server.entity.KbLintIssue;
import com.moli.knowledge.server.entity.KbSyncLog;
import com.moli.knowledge.server.mapper.KbLintIssueMapper;
import com.moli.knowledge.server.mapper.KbRelationMapper;
import com.moli.knowledge.server.mapper.KbSyncLogMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbLlmConfigService;
import com.moli.knowledge.server.service.KbOpsService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbOpsServiceImplTest {

    @InjectMocks
    private KbOpsServiceImpl service;

    @Mock
    private KbAclService kbAclService;
    @Mock
    private KbSyncLogMapper kbSyncLogMapper;
    @Mock
    private KbLintIssueMapper kbLintIssueMapper;
    @Mock
    private KbRelationMapper kbRelationMapper;
    @Mock
    private KbLlmConfigService kbLlmConfigService;

    @Before
    public void setUp() {
        when(kbAclService.accessibleSpaceIds()).thenReturn(Collections.singletonList(1L));
        when(kbSyncLogMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(kbLintIssueMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(kbRelationMapper.selectCount(any())).thenReturn(0);
        KbLlmConfigVo llm = new KbLlmConfigVo();
        llm.setAvailable(true);
        llm.setConfigEnabled(true);
        llm.setProvider("glm");
        llm.setModel("glm-4-flash");
        when(kbLlmConfigService.getConfig()).thenReturn(llm);
    }

    @Test
    public void dashboard_returnsTrendDays() {
        KbSyncLog log = new KbSyncLog();
        log.setBatchNo("b1");
        log.setStatus("success");
        log.setSpaceId(1L);
        log.setCreateTime(new Date());
        when(kbSyncLogMapper.selectList(any())).thenReturn(Collections.singletonList(log));

        KbLintIssue issue = new KbLintIssue();
        issue.setStatus(0);
        issue.setIssueType("broken_link");
        issue.setDetail("a -> [[b]]");
        issue.setSpaceId(1L);
        when(kbLintIssueMapper.selectList(any())).thenReturn(Collections.singletonList(issue));

        KbOpsDashboardVo vo = service.dashboard(null, 7);
        Assert.assertEquals(7, vo.getSyncTrend().size());
        Assert.assertEquals(1, vo.getLintSummary().getOpenCount());
        Assert.assertTrue(vo.getLlm().getAvailable());
        verify(kbAclService).assertCanOpsDashboard(null);
    }
}
