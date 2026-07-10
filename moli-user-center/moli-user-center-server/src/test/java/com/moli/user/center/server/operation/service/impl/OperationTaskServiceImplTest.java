package com.moli.user.center.server.operation.service.impl;

import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.OperationTask;
import com.moli.user.center.common.domain.vo.OperationTaskVo;
import com.moli.user.center.server.operation.config.OperationTaskProperties;
import com.moli.user.center.server.operation.mapper.OperationTaskMapper;
import com.moli.user.center.server.operation.task.OperationTaskStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationTaskServiceImplTest {

    @InjectMocks
    private OperationTaskServiceImpl operationTaskService;

    @Mock
    private OperationTaskMapper operationTaskMapper;

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
