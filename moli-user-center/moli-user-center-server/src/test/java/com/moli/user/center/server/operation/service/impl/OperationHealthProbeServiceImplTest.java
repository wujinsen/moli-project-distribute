package com.moli.user.center.server.operation.service.impl;

import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationTask;
import com.moli.user.center.common.domain.vo.OperationHealthProbeResultVo;
import com.moli.user.center.server.operation.config.OperationHealthProperties;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.service.OperationDeployStatusSyncService;
import com.moli.user.center.server.operation.service.OperationProjectService;
import com.moli.user.center.server.operation.service.OperationTaskService;
import com.moli.user.center.server.operation.support.OperationHealthProbeExecutor;
import com.moli.user.center.server.operation.support.OperationMapperBatchSupport;
import com.moli.user.center.server.operation.task.OperationTaskContext;
import com.moli.user.center.server.operation.task.OperationTaskJob;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationHealthProbeServiceImplTest {

    @InjectMocks
    private OperationHealthProbeServiceImpl healthProbeService;

    @Mock
    private OperationServerMapper operationServerMapper;
    @Mock
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;
    @Mock
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Mock
    private OperationProjectService operationProjectService;
    @Mock
    private OperationDeployStatusSyncService operationDeployStatusSyncService;
    @Mock
    private OperationTaskService operationTaskService;
    @Mock
    private OperationHealthProbeExecutor healthProbeExecutor;
    @Mock
    private OperationMapperBatchSupport mapperBatchSupport;
    @Mock
    private OperationHealthProperties healthProperties;

    @Before
    public void setUp() {
        when(healthProbeExecutor.getExecutor()).thenReturn(Executors.newSingleThreadExecutor());
        when(healthProperties.getProbeTimeoutSeconds()).thenReturn(120);
    }

    @Test
    public void probeAll_syncs_deploy_status_per_project() {
        when(operationServerMapper.selectList(null)).thenReturn(Collections.emptyList());
        when(operationComponentDeployInfoMapper.selectList(null)).thenReturn(Collections.emptyList());

        OperationProjectDeployInfo project = new OperationProjectDeployInfo();
        project.setId(1L);
        project.setProjectName("moli-server");
        project.setServerId(204L);
        when(operationProjectDeployInfoMapper.selectList(null)).thenReturn(Collections.singletonList(project));
        when(operationDeployStatusSyncService.syncProject(project)).thenAnswer(invocation -> {
            project.setDeployRunning(true);
            return true;
        });

        OperationHealthProbeResultVo result = healthProbeService.probeAll();

        assertEquals(1, result.getDeployStatusesSynced());
        verify(mapperBatchSupport).updateBatchById(eq(OperationProjectDeployInfoMapper.class), any());
        verify(operationProjectDeployInfoMapper, never()).updateById(any());
    }

    @Test
    public void probeAll_skips_db_update_when_sync_returns_false() {
        when(operationServerMapper.selectList(null)).thenReturn(Collections.emptyList());
        when(operationComponentDeployInfoMapper.selectList(null)).thenReturn(Collections.emptyList());

        OperationProjectDeployInfo project = new OperationProjectDeployInfo();
        project.setProjectName("moli-admin");
        when(operationProjectDeployInfoMapper.selectList(null)).thenReturn(Collections.singletonList(project));
        when(operationDeployStatusSyncService.syncProject(project)).thenReturn(false);

        OperationHealthProbeResultVo result = healthProbeService.probeAll();

        assertEquals(0, result.getDeployStatusesSynced());
        verify(mapperBatchSupport, never()).updateBatchById(eq(OperationProjectDeployInfoMapper.class), any());
    }

    @Test
    public void createProbeAllTask_returns_task_id() {
        OperationTask task = new OperationTask();
        task.setId(77L);
        when(operationTaskService.create(eq("health_probe"), eq(null), eq(null), eq(null), eq("probe-all"), anyString()))
                .thenReturn(task);
        when(operationServerMapper.selectList(null)).thenReturn(Collections.emptyList());
        when(operationComponentDeployInfoMapper.selectList(null)).thenReturn(Collections.emptyList());
        when(operationProjectDeployInfoMapper.selectList(null)).thenReturn(Collections.emptyList());
        doAnswer(invocation -> {
            OperationTaskJob job = invocation.getArgument(2);
            job.run(mock(OperationTaskContext.class));
            return null;
        }).when(operationTaskService).submit(eq(77L), eq("health_probe:global"), any());

        assertEquals(Long.valueOf(77L), healthProbeService.createProbeAllTask());
    }
}
