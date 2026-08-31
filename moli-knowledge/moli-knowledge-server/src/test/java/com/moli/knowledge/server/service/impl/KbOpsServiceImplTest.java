package com.moli.knowledge.server.service.impl;

import com.moli.knowledge.server.config.KbEvalBaselinesProvider;
import com.moli.knowledge.server.dto.KbLlmConfigVo;
import com.moli.knowledge.server.dto.KbOpsDriftSummaryVo;
import com.moli.knowledge.server.dto.KbOpsDashboardVo;
import com.moli.knowledge.server.entity.KbLintIssue;
import com.moli.knowledge.server.entity.KbLlmCallLog;
import com.moli.knowledge.server.entity.KbSyncLog;
import com.moli.knowledge.server.llm.KbLlmConfigSource;
import com.moli.knowledge.server.llm.KbLlmRuntime;
import com.moli.knowledge.server.mapper.KbEvalRunMapper;
import com.moli.knowledge.server.mapper.KbLintIssueMapper;
import com.moli.knowledge.server.mapper.KbLlmCallLogMapper;
import com.moli.knowledge.server.mapper.KbRelationMapper;
import com.moli.knowledge.server.mapper.KbSyncLogMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbDriftService;
import com.moli.knowledge.server.service.KbLlmConfigService;
import com.moli.knowledge.server.service.KbLlmCallLogService;
import com.moli.knowledge.server.service.KbPlatformLlmConfigService;
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
    @Mock
    private KbLlmCallLogService kbLlmCallLogService;
    @Mock
    private KbPlatformLlmConfigService kbPlatformLlmConfigService;
    @Mock
    private KbLlmRuntime kbLlmRuntime;
    @Mock
    private KbDriftService kbDriftService;
    @Mock
    private KbEvalRunMapper kbEvalRunMapper;
    @Mock
    private KbEvalBaselinesProvider kbEvalBaselinesProvider;

    @Before
    public void setUp() {
        when(kbAclService.accessibleSpaceIds()).thenReturn(Collections.singletonList(1L));
        when(kbSyncLogMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(kbLintIssueMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(kbRelationMapper.selectCount(any())).thenReturn(0);
        when(kbEvalRunMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(kbEvalRunMapper.selectOne(any())).thenReturn(null);
        when(kbEvalBaselinesProvider.strategyKeys()).thenReturn(
                java.util.Arrays.asList("ngram", "hybrid", "hybrid-rerank"));
        when(kbEvalBaselinesProvider.goldenTotalFromBaselines()).thenReturn(59);
        KbLlmConfigVo llm = new KbLlmConfigVo();
        llm.setAvailable(true);
        llm.setConfigEnabled(true);
        llm.setProvider("glm");
        llm.setModel("glm-4-flash");
        when(kbLlmConfigService.getConfig()).thenReturn(llm);
        when(kbLlmRuntime.getSource()).thenReturn(KbLlmConfigSource.DATABASE);
        when(kbPlatformLlmConfigService.isCallLogEnabled()).thenReturn(true);

        KbLlmCallLogService.LlmCallStats stats = new KbLlmCallLogService.LlmCallStats();
        stats.setTotalCalls(10);
        stats.setSuccessCalls(8);
        stats.setFailCalls(2);
        when(kbLlmCallLogService.aggregate(any(), org.mockito.ArgumentMatchers.anyBoolean(), any(Integer.class))).thenReturn(stats);
        KbOpsDriftSummaryVo drift = new KbOpsDriftSummaryVo();
        drift.setDrifted(false);
        when(kbDriftService.driftSummary(null, 5)).thenReturn(drift);
    }

    @Test
    public void dashboard_includesDriftSummary() {
        KbOpsDriftSummaryVo drift = new KbOpsDriftSummaryVo();
        drift.setDrifted(true);
        drift.setWikiOnlyTotal(3);
        when(kbDriftService.driftSummary(null, 5)).thenReturn(drift);

        KbOpsDashboardVo vo = service.dashboard(null, 7);
        Assert.assertNotNull(vo.getDriftSummary());
        Assert.assertTrue(vo.getDriftSummary().isDrifted());
        Assert.assertEquals(3, vo.getDriftSummary().getWikiOnlyTotal());
    }

    @Test
    public void dashboard_returnsTrendDaysAndLlmStats() {
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
        Assert.assertEquals(10, vo.getLlm().getTotalCalls());
        Assert.assertEquals(0.8, vo.getLlm().getSuccessRate(), 0.001);
        Assert.assertNotNull(vo.getRetrievalQuality());
        Assert.assertEquals(3, vo.getRetrievalQuality().getStrategies().size());
        verify(kbAclService).assertCanOpsDashboard(null);
    }
}
