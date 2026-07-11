package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.LintIssueBatchAssignRequest;
import com.moli.knowledge.server.dto.LintIssueBatchStatusRequest;
import com.moli.knowledge.server.dto.LintIssueTypeVo;
import com.moli.knowledge.server.service.KbInsightService;
import com.moli.knowledge.server.testsupport.ControllerTestSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbInsightControllerLintOpsApiTest {

    @InjectMocks
    private KbInsightController controller;

    @Mock
    private KbInsightService kbInsightService;

    @Test
    public void PUT_kb_lint_issues_batch_assign() {
        LintIssueBatchAssignRequest request = new LintIssueBatchAssignRequest();
        request.setIds(Arrays.asList(1L, 2L));
        request.setAssigneeId(99L);
        request.setPriority(1);
        when(kbInsightService.batchAssignIssues(request)).thenReturn(2);

        MoliResult<Integer> result = controller.batchAssignIssues(request);

        ControllerTestSupport.assertSuccess(result);
        Assert.assertEquals(Integer.valueOf(2), result.getData());
        verify(kbInsightService).batchAssignIssues(request);
    }

    @Test
    public void GET_kb_lint_issue_types() {
        MoliResult<List<LintIssueTypeVo>> result = controller.issueTypes();

        ControllerTestSupport.assertSuccess(result);
        Assert.assertTrue(result.getData().stream().anyMatch(t -> "broken_link".equals(t.getCode())));
        Assert.assertTrue(result.getData().stream().anyMatch(t -> "missing_source".equals(t.getCode())));
        Assert.assertTrue(result.getData().stream().anyMatch(t -> t.isLintPyOnly() && "space_branding".equals(t.getLintPyKind())));
    }

    @Test
    public void PUT_kb_lint_issues_batch_status() {
        LintIssueBatchStatusRequest request = new LintIssueBatchStatusRequest();
        request.setIds(Arrays.asList(1L, 2L));
        request.setStatus(2);
        when(kbInsightService.batchUpdateIssueStatus(request)).thenReturn(2);

        MoliResult<Integer> result = controller.batchUpdateIssueStatus(request);

        ControllerTestSupport.assertSuccess(result);
        Assert.assertEquals(Integer.valueOf(2), result.getData());
    }

    @Test
    public void PUT_kb_lint_issue_assign() {
        MoliResult<Boolean> result = controller.assignIssue(1L, 99L, 1);
        ControllerTestSupport.assertSuccess(result);
        verify(kbInsightService).assignIssue(1L, 99L, 1);
    }

    @Test
    public void PUT_kb_lint_issue_status() {
        MoliResult<Boolean> result = controller.updateIssue(1L, 2);
        ControllerTestSupport.assertSuccess(result);
        verify(kbInsightService).updateIssueStatus(1L, 2);
    }

    @Test
    public void GET_kb_lint_scan_status() {
        com.moli.knowledge.server.dto.LintScanStatusVo status = new com.moli.knowledge.server.dto.LintScanStatusVo();
        status.setScheduleEnabled(true);
        when(kbInsightService.scanStatus(100L)).thenReturn(status);

        MoliResult<com.moli.knowledge.server.dto.LintScanStatusVo> result = controller.scanStatus(100L);

        ControllerTestSupport.assertSuccess(result);
        Assert.assertTrue(result.getData().isScheduleEnabled());
        verify(kbInsightService).scanStatus(100L);
    }
}
