package com.moli.user.center.server.operation.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationTask;
import com.moli.user.center.common.domain.vo.OperationTaskProjectGroupVo;
import com.moli.user.center.common.domain.vo.OperationTaskVo;
import com.moli.user.center.server.operation.config.OperationTaskProperties;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationTaskMapper;
import com.moli.user.center.server.operation.task.OperationTaskStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationTaskServiceImplTest {

    @InjectMocks
    private OperationTaskServiceImpl operationTaskService;

    @Mock
    private OperationTaskMapper operationTaskMapper;

    @Mock
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;

    @Mock
    private OperationTaskProperties taskProperties;

    @Before
    public void setUp() {
        when(taskProperties.getPoolSize()).thenReturn(1);
        when(taskProperties.getLogMaxChars()).thenReturn(100_000);
        operationTaskService.init();
    }

    @Test
    public void poll_returns_incremental_log_from_offset() {
        OperationTask row = taskRow(1L, OperationTaskStatus.SUCCESS, 100, "line1\nline2\n");
        when(operationTaskMapper.selectById(1L)).thenReturn(row);

        OperationTaskVo vo = operationTaskService.poll(1L, 6);

        assertEquals("line2\n", vo.getLogChunk());
        assertEquals(Integer.valueOf(12), vo.getNextLogOffset());
        assertTrue(vo.getFinished());
        assertEquals(OperationTaskStatus.SUCCESS, vo.getStatus());
    }

    @Test
    public void poll_clamps_negative_offset_to_zero() {
        OperationTask row = taskRow(2L, OperationTaskStatus.RUNNING, 50, "abc");
        when(operationTaskMapper.selectById(2L)).thenReturn(row);

        OperationTaskVo vo = operationTaskService.poll(2L, -10);

        assertEquals("abc", vo.getLogChunk());
        assertEquals(Integer.valueOf(3), vo.getNextLogOffset());
        assertFalse(vo.getFinished());
    }

    @Test
    public void poll_clamps_offset_beyond_log_length() {
        OperationTask row = taskRow(3L, OperationTaskStatus.FAILED, 0, "done");
        when(operationTaskMapper.selectById(3L)).thenReturn(row);

        OperationTaskVo vo = operationTaskService.poll(3L, 999);

        assertEquals("", vo.getLogChunk());
        assertEquals(Integer.valueOf(4), vo.getNextLogOffset());
        assertTrue(vo.getFinished());
    }

    @Test(expected = BaseException.class)
    public void poll_throws_when_task_missing() {
        when(operationTaskMapper.selectById(404L)).thenReturn(null);
        operationTaskService.poll(404L, 0);
    }

    @Test
    public void cancel_marks_pending_task_cancelled_before_run() throws Exception {
        OperationTask row = taskRow(10L, OperationTaskStatus.PENDING, 0, "");
        when(operationTaskMapper.selectById(10L)).thenReturn(row);

        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        operationTaskService.submit(10L, "lock:test", context -> {
            started.countDown();
            release.await(5, TimeUnit.SECONDS);
        });

        started.await(3, TimeUnit.SECONDS);
        OperationTaskVo cancelled = operationTaskService.cancel(10L);
        assertEquals(OperationTaskStatus.CANCELLED, cancelled.getStatus());
        assertTrue(cancelled.getFinished());
        release.countDown();
        Thread.sleep(200);
    }

    @Test(expected = BaseException.class)
    public void cancel_rejects_finished_task() {
        OperationTask row = taskRow(11L, OperationTaskStatus.SUCCESS, 100, "");
        when(operationTaskMapper.selectById(11L)).thenReturn(row);
        operationTaskService.cancel(11L);
    }

    @Test
    public void listGroups_groups_by_project_and_computes_aggregates() {
        Date older = new Date(1_000L);
        Date newer = new Date(2_000L);
        when(operationTaskMapper.selectList(any())).thenReturn(Arrays.asList(
                listTask(1L, 401L, OperationTaskStatus.SUCCESS, older),
                listTask(2L, 401L, OperationTaskStatus.RUNNING, newer),
                listTask(3L, null, OperationTaskStatus.FAILED, newer)
        ));

        OperationProjectDeployInfo project = new OperationProjectDeployInfo();
        project.setId(401L);
        project.setProjectName("moli-user-center");
        when(operationProjectDeployInfoMapper.selectBatchIds(any()))
                .thenReturn(Collections.singletonList(project));

        PageRes<OperationTaskProjectGroupVo> result = operationTaskService.listGroups(
                null, null, null, null, 1, 10, 20);

        assertEquals(2, result.getTotal().intValue());
        assertEquals(2, result.getList().size());

        OperationTaskProjectGroupVo projectGroup = result.getList().get(0);
        assertEquals(Long.valueOf(401L), projectGroup.getProjectId());
        assertEquals("moli-user-center", projectGroup.getProjectName());
        assertEquals(Integer.valueOf(2), projectGroup.getTaskCount());
        assertEquals(Integer.valueOf(1), projectGroup.getRunningCount());
        assertEquals(Integer.valueOf(1), projectGroup.getSuccessCount());
        assertEquals(Integer.valueOf(0), projectGroup.getFailedCount());
        assertEquals(newer, projectGroup.getLatestCreateTime());
        assertEquals(2, projectGroup.getTasks().size());
        assertEquals(Long.valueOf(2L), projectGroup.getTasks().get(0).getId());

        OperationTaskProjectGroupVo unassigned = result.getList().get(1);
        assertNull(unassigned.getProjectId());
        assertNull(unassigned.getProjectName());
        assertEquals(Integer.valueOf(1), unassigned.getTaskCount());
        assertEquals(Integer.valueOf(1), unassigned.getFailedCount());
    }

    @Test
    public void listGroups_paginates_groups_and_limits_tasks_per_group() {
        Date t1 = new Date(3_000L);
        Date t2 = new Date(2_000L);
        Date t3 = new Date(1_000L);
        when(operationTaskMapper.selectList(any())).thenReturn(Arrays.asList(
                listTask(10L, 501L, OperationTaskStatus.SUCCESS, t1),
                listTask(11L, 501L, OperationTaskStatus.PENDING, t2),
                listTask(12L, 502L, OperationTaskStatus.SUCCESS, t3)
        ));
        when(operationProjectDeployInfoMapper.selectBatchIds(any())).thenReturn(Collections.emptyList());

        PageRes<OperationTaskProjectGroupVo> page1 = operationTaskService.listGroups(
                null, null, null, null, 1, 1, 1);
        assertEquals(2, page1.getTotal().intValue());
        assertEquals(1, page1.getList().size());
        assertEquals(Long.valueOf(501L), page1.getList().get(0).getProjectId());
        assertEquals(1, page1.getList().get(0).getTasks().size());
        assertEquals(Long.valueOf(10L), page1.getList().get(0).getTasks().get(0).getId());

        PageRes<OperationTaskProjectGroupVo> page2 = operationTaskService.listGroups(
                null, null, null, null, 2, 1, 1);
        assertEquals(1, page2.getList().size());
        assertEquals(Long.valueOf(502L), page2.getList().get(0).getProjectId());
    }

    @Test
    public void listGroups_returns_empty_when_no_tasks() {
        when(operationTaskMapper.selectList(any())).thenReturn(Collections.emptyList());

        PageRes<OperationTaskProjectGroupVo> result = operationTaskService.listGroups(
                null, null, null, null, 1, 10, 20);

        assertEquals(Integer.valueOf(0), result.getTotal());
        assertTrue(result.getList().isEmpty());
        assertEquals(Integer.valueOf(1), result.getPageNum());
        assertEquals(Integer.valueOf(10), result.getPageSize());
    }

    @Test
    public void listGroups_pending_counts_as_running() {
        when(operationTaskMapper.selectList(any())).thenReturn(Collections.singletonList(
                listTask(1L, 601L, OperationTaskStatus.PENDING, new Date(1_000L))
        ));
        when(operationProjectDeployInfoMapper.selectBatchIds(any())).thenReturn(Collections.emptyList());

        OperationTaskProjectGroupVo group = operationTaskService.listGroups(
                null, null, null, null, 1, 10, 20).getList().get(0);

        assertEquals(Integer.valueOf(1), group.getRunningCount());
        assertEquals(Integer.valueOf(0), group.getSuccessCount());
        assertEquals(Integer.valueOf(0), group.getFailedCount());
    }

    @Test
    public void listGroups_cancelled_counts_in_taskCount_only() {
        when(operationTaskMapper.selectList(any())).thenReturn(Collections.singletonList(
                listTask(1L, 602L, OperationTaskStatus.CANCELLED, new Date(1_000L))
        ));
        when(operationProjectDeployInfoMapper.selectBatchIds(any())).thenReturn(Collections.emptyList());

        OperationTaskProjectGroupVo group = operationTaskService.listGroups(
                null, null, null, null, 1, 10, 20).getList().get(0);

        assertEquals(Integer.valueOf(1), group.getTaskCount());
        assertEquals(Integer.valueOf(0), group.getRunningCount());
        assertEquals(Integer.valueOf(0), group.getSuccessCount());
        assertEquals(Integer.valueOf(0), group.getFailedCount());
        assertTrue(group.getTasks().get(0).getFinished());
    }

    @Test
    public void listGroups_page_beyond_total_returns_empty_list() {
        when(operationTaskMapper.selectList(any())).thenReturn(Collections.singletonList(
                listTask(1L, 603L, OperationTaskStatus.SUCCESS, new Date(1_000L))
        ));
        when(operationProjectDeployInfoMapper.selectBatchIds(any())).thenReturn(Collections.emptyList());

        PageRes<OperationTaskProjectGroupVo> result = operationTaskService.listGroups(
                null, null, null, null, 99, 10, 20);

        assertEquals(Integer.valueOf(1), result.getTotal());
        assertTrue(result.getList().isEmpty());
        assertEquals(Integer.valueOf(99), result.getPageNum());
    }

    @Test
    public void listGroups_single_project_filter_returns_one_group() {
        when(operationTaskMapper.selectList(any())).thenReturn(Arrays.asList(
                listTask(1L, 701L, OperationTaskStatus.SUCCESS, new Date(2_000L)),
                listTask(2L, 701L, OperationTaskStatus.FAILED, new Date(1_000L))
        ));
        OperationProjectDeployInfo project = new OperationProjectDeployInfo();
        project.setId(701L);
        project.setProjectName("gateway");
        when(operationProjectDeployInfoMapper.selectBatchIds(any()))
                .thenReturn(Collections.singletonList(project));

        PageRes<OperationTaskProjectGroupVo> result = operationTaskService.listGroups(
                null, null, 701L, null, 1, 10, 20);

        assertEquals(Integer.valueOf(1), result.getTotal());
        OperationTaskProjectGroupVo group = result.getList().get(0);
        assertEquals(Long.valueOf(701L), group.getProjectId());
        assertEquals("gateway", group.getProjectName());
        assertEquals(Integer.valueOf(2), group.getTaskCount());
        assertEquals(Integer.valueOf(1), group.getSuccessCount());
        assertEquals(Integer.valueOf(1), group.getFailedCount());
        assertEquals(Long.valueOf(1L), group.getTasks().get(0).getId());
    }

    @Test
    public void listGroups_tasks_sorted_by_create_time_desc_within_group() {
        Date oldest = new Date(1_000L);
        Date middle = new Date(2_000L);
        Date newest = new Date(3_000L);
        when(operationTaskMapper.selectList(any())).thenReturn(Arrays.asList(
                listTask(1L, 801L, OperationTaskStatus.SUCCESS, oldest),
                listTask(2L, 801L, OperationTaskStatus.SUCCESS, newest),
                listTask(3L, 801L, OperationTaskStatus.SUCCESS, middle)
        ));
        when(operationProjectDeployInfoMapper.selectBatchIds(any())).thenReturn(Collections.emptyList());

        OperationTaskProjectGroupVo group = operationTaskService.listGroups(
                null, null, null, null, 1, 10, 20).getList().get(0);

        assertEquals(Long.valueOf(2L), group.getTasks().get(0).getId());
        assertEquals(Long.valueOf(3L), group.getTasks().get(1).getId());
        assertEquals(Long.valueOf(1L), group.getTasks().get(2).getId());
    }

    private static OperationTask listTask(Long id, Long projectId, String status, Date createTime) {
        OperationTask task = new OperationTask();
        task.setId(id);
        task.setTaskType("deploy");
        task.setProjectId(projectId);
        task.setStatus(status);
        task.setProgress(50);
        task.setCreateTime(createTime);
        return task;
    }

    private static OperationTask taskRow(Long id, String status, int progress, String log) {
        OperationTask task = new OperationTask();
        task.setId(id);
        task.setTaskType("deploy");
        task.setStatus(status);
        task.setProgress(progress);
        task.setTaskLog(log);
        return task;
    }
}
