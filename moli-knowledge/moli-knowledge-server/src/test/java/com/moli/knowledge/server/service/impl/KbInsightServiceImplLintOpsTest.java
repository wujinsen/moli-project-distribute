package com.moli.knowledge.server.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.dto.LintIssueBatchAssignRequest;
import com.moli.knowledge.server.entity.KbLintIssue;
import com.moli.knowledge.server.mapper.KbLintIssueMapper;
import com.moli.knowledge.server.service.KbAclService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KbInsightServiceImplLintOpsTest {

    private static final Long SPACE_ID = 100L;
    private static final Long ISSUE_ID = 9001L;
    private static final Long ASSIGNEE_ID = 42L;

    @InjectMocks
    private KbInsightServiceImpl service;

    @Mock
    private KbLintIssueMapper kbLintIssueMapper;
    @Mock
    private KbAclService kbAclService;

    @Test
    public void batchAssignIssues_updatesAssigneeAndPriority() {
        KbLintIssue issue = openIssue();
        when(kbLintIssueMapper.selectById(ISSUE_ID)).thenReturn(issue);

        LintIssueBatchAssignRequest request = new LintIssueBatchAssignRequest();
        request.setIds(Collections.singletonList(ISSUE_ID));
        request.setAssigneeId(ASSIGNEE_ID);
        request.setPriority(2);

        int updated = service.batchAssignIssues(request);

        Assert.assertEquals(1, updated);
        ArgumentCaptor<KbLintIssue> captor = ArgumentCaptor.forClass(KbLintIssue.class);
        verify(kbLintIssueMapper).updateById(captor.capture());
        Assert.assertEquals(ASSIGNEE_ID, captor.getValue().getAssigneeId());
        Assert.assertEquals(Integer.valueOf(2), captor.getValue().getPriority());
        verify(kbAclService).assertCanEdit(SPACE_ID);
    }

    @Test
    public void batchAssignIssues_skipsMissingIds() {
        when(kbLintIssueMapper.selectById(ISSUE_ID)).thenReturn(null);

        LintIssueBatchAssignRequest request = new LintIssueBatchAssignRequest();
        request.setIds(Collections.singletonList(ISSUE_ID));
        request.setAssigneeId(ASSIGNEE_ID);

        Assert.assertEquals(0, service.batchAssignIssues(request));
    }

    @Test(expected = BaseException.class)
    public void batchAssignIssues_rejectsEmptyIds() {
        LintIssueBatchAssignRequest request = new LintIssueBatchAssignRequest();
        request.setAssigneeId(ASSIGNEE_ID);
        service.batchAssignIssues(request);
    }

    @Test(expected = BaseException.class)
    public void batchAssignIssues_requiresAssigneeOrPriority() {
        LintIssueBatchAssignRequest request = new LintIssueBatchAssignRequest();
        request.setIds(Collections.singletonList(ISSUE_ID));
        service.batchAssignIssues(request);
    }

    @Test(expected = BaseException.class)
    public void assignIssue_rejectsInvalidPriority() {
        when(kbLintIssueMapper.selectById(ISSUE_ID)).thenReturn(openIssue());
        service.assignIssue(ISSUE_ID, null, 9);
    }

    @Test
    public void assignIssue_updatesPriorityOnly() {
        KbLintIssue issue = openIssue();
        when(kbLintIssueMapper.selectById(ISSUE_ID)).thenReturn(issue);

        service.assignIssue(ISSUE_ID, null, 1);

        ArgumentCaptor<KbLintIssue> captor = ArgumentCaptor.forClass(KbLintIssue.class);
        verify(kbLintIssueMapper).updateById(captor.capture());
        Assert.assertEquals(Integer.valueOf(1), captor.getValue().getPriority());
    }

    private static KbLintIssue openIssue() {
        KbLintIssue issue = new KbLintIssue();
        issue.setId(ISSUE_ID);
        issue.setSpaceId(SPACE_ID);
        issue.setStatus(0);
        issue.setPriority(0);
        return issue;
    }
}
