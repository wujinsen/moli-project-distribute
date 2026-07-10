package com.moli.knowledge.server.controller;

import com.moli.common.core.MoliResult;
import com.moli.knowledge.server.dto.LintIssueBatchAssignRequest;
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
        MoliResult<List<String>> result = controller.issueTypes();

        ControllerTestSupport.assertSuccess(result);
        Assert.assertTrue(result.getData().contains("broken_link"));
        Assert.assertTrue(result.getData().contains("missing_source"));
    }
}
