package com.moli.user.center.server.operation.service.impl;



import com.moli.common.exception.BaseException;

import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;

import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;

import com.moli.user.center.common.domain.entity.OperationServerInfo;

import com.moli.user.center.common.domain.entity.OperationTask;

import com.moli.user.center.common.domain.vo.OperationHealthProbeResultVo;

import com.moli.user.center.server.operation.config.OperationHealthProperties;

import com.moli.user.center.server.operation.health.OperationTcpProbe;

import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;

import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;

import com.moli.user.center.server.operation.mapper.OperationServerMapper;

import com.moli.user.center.server.operation.service.OperationDeployStatusSyncService;

import com.moli.user.center.server.operation.service.OperationHealthProbeService;

import com.moli.user.center.server.operation.service.OperationProjectService;

import com.moli.user.center.server.operation.service.OperationTaskService;

import com.moli.user.center.server.operation.support.OperationHealthProbeExecutor;

import com.moli.user.center.server.operation.support.OperationMapperBatchSupport;

import com.moli.user.center.server.operation.task.OperationTaskCancelledException;
import com.moli.user.center.server.operation.task.OperationTaskContext;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Service;



import javax.annotation.Resource;

import java.util.ArrayList;

import java.util.Date;

import java.util.List;

import java.util.concurrent.CountDownLatch;

import java.util.concurrent.ExecutorService;

import java.util.concurrent.TimeUnit;



@Service

@Slf4j

public class OperationHealthProbeServiceImpl implements OperationHealthProbeService {



    private static final String TASK_TYPE = "health_probe";

    private static final String LOCK_KEY = "health_probe:global";



    @Resource

    private OperationServerMapper operationServerMapper;

    @Resource

    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;

    @Resource

    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;

    @Resource

    private OperationProjectService operationProjectService;

    @Resource

    private OperationDeployStatusSyncService operationDeployStatusSyncService;

    @Resource

    private OperationTaskService operationTaskService;

    @Resource

    private OperationHealthProbeExecutor healthProbeExecutor;

    @Resource

    private OperationMapperBatchSupport mapperBatchSupport;

    @Resource

    private OperationHealthProperties healthProperties;



    @Override

    public OperationHealthProbeResultVo probeAll() {
        try {
            return executeProbeAll(null);
        } catch (OperationTaskCancelledException e) {
            throw new IllegalStateException("sync probe cannot be cancelled", e);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }



    @Override

    public Long createProbeAllTask() {

        OperationTask task = operationTaskService.create(
                TASK_TYPE, null, null, null, "probe-all", "批量 TCP 探活 + 部署状态同步");

        operationTaskService.submit(task.getId(), LOCK_KEY, context -> executeProbeAll(context));

        return task.getId();

    }



    private OperationHealthProbeResultVo executeProbeAll(OperationTaskContext context) throws Exception {

        OperationHealthProbeResultVo result = new OperationHealthProbeResultVo();

        append(context, "[PROBE] 开始批量探活...");



        List<OperationServerInfo> servers = operationServerMapper.selectList(null);

        probeServersInParallel(servers, context);

        mapperBatchSupport.updateBatchById(OperationServerMapper.class, servers);

        result.setServersProbed(servers.size());

        setProgress(context, 40);

        append(context, "[PROBE] 服务器探活完成: " + servers.size());

        throwIfCancelled(context);

        List<OperationComponentDeployInfo> components = operationComponentDeployInfoMapper.selectList(null);

        probeComponentsInParallel(components, context);

        mapperBatchSupport.updateBatchById(OperationComponentDeployInfoMapper.class, components);

        result.setComponentsProbed(components.size());

        setProgress(context, 70);

        append(context, "[PROBE] 组件探活完成: " + components.size());

        throwIfCancelled(context);

        List<OperationProjectDeployInfo> projects = operationProjectDeployInfoMapper.selectList(null);

        List<OperationProjectDeployInfo> serverIdUpdates = syncProjectServerIds(projects, result, context);

        if (!serverIdUpdates.isEmpty()) {

            mapperBatchSupport.updateBatchById(OperationProjectDeployInfoMapper.class, serverIdUpdates);

        }

        setProgress(context, 85);



        List<OperationProjectDeployInfo> deployUpdates = syncDeployStatuses(projects, result, context);

        if (!deployUpdates.isEmpty()) {

            mapperBatchSupport.updateBatchById(OperationProjectDeployInfoMapper.class, deployUpdates);

        }

        setProgress(context, 100);

        append(context, String.format(

                "[DONE] servers=%d components=%d serverIdsSynced=%d deploySynced=%d",

                result.getServersProbed(), result.getComponentsProbed(),

                result.getServerIdsSynced(), result.getDeployStatusesSynced()));

        return result;

    }



    private void probeServersInParallel(List<OperationServerInfo> servers, OperationTaskContext context) {

        awaitParallelProbes(servers.size(), index -> {

            OperationServerInfo server = servers.get(index);

            server.setStatus(OperationTcpProbe.probe(server.getIp(), server.getPort()));

            server.setLastCheckTime(new Date());

        }, context, "服务器");

    }



    private void probeComponentsInParallel(List<OperationComponentDeployInfo> components,

                                             OperationTaskContext context) {

        awaitParallelProbes(components.size(), index -> {

            OperationComponentDeployInfo component = components.get(index);

            component.setStatus(OperationTcpProbe.probe(component.getServerIp(), component.getPort()));

            component.setLastCheckTime(new Date());

        }, context, "组件");

    }



    private void awaitParallelProbes(int size, ProbeAction action, OperationTaskContext context, String label) {

        if (size == 0) {

            return;

        }

        ExecutorService executor = healthProbeExecutor.getExecutor();

        CountDownLatch latch = new CountDownLatch(size);

        for (int i = 0; i < size; i++) {

            final int index = i;

            executor.submit(() -> {

                try {

                    action.run(index);

                } catch (Exception ex) {

                    log.warn("{} probe failed at index {}: {}", label, index, ex.getMessage());

                } finally {

                    latch.countDown();

                }

            });

        }

        try {

            boolean finished = latch.await(healthProperties.getProbeTimeoutSeconds(), TimeUnit.SECONDS);

            if (!finished) {

                throw new BaseException(label + "探活超时（>" + healthProperties.getProbeTimeoutSeconds() + "s）");

            }

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            throw new BaseException(label + "探活被中断");

        }

    }



    private List<OperationProjectDeployInfo> syncProjectServerIds(List<OperationProjectDeployInfo> projects,

                                                                    OperationHealthProbeResultVo result,

                                                                    OperationTaskContext context) {

        List<OperationProjectDeployInfo> updates = new ArrayList<>();

        for (OperationProjectDeployInfo project : projects) {

            if (project.getServerId() == null && StringUtils.isNotBlank(project.getServerIp())) {

                operationProjectService.syncServerIdFromIp(project);

                if (project.getServerId() != null) {

                    updates.add(project);

                    result.setServerIdsSynced(result.getServerIdsSynced() + 1);

                }

            }

        }

        append(context, "[SYNC] serverId 回填: " + result.getServerIdsSynced());

        return updates;

    }



    private List<OperationProjectDeployInfo> syncDeployStatuses(List<OperationProjectDeployInfo> projects,

                                                                  OperationHealthProbeResultVo result,

                                                                  OperationTaskContext context) {

        List<OperationProjectDeployInfo> updates = new ArrayList<>();

        for (OperationProjectDeployInfo project : projects) {

            try {

                if (operationDeployStatusSyncService.syncProject(project)) {

                    updates.add(project);

                    result.setDeployStatusesSynced(result.getDeployStatusesSynced() + 1);

                }

            } catch (Exception ex) {

                log.warn("deploy status sync failed for project {}: {}",

                        project.getProjectName(), ex.getMessage());

            }

        }

        append(context, "[SYNC] deploy_running: " + result.getDeployStatusesSynced());

        return updates;

    }



    private static void append(OperationTaskContext context, String line) {

        if (context != null) {

            context.appendLine(line);

        }

    }



    private static void setProgress(OperationTaskContext context, int progress) {

        if (context != null) {

            context.setProgress(progress);

        }

    }

    private static void throwIfCancelled(OperationTaskContext context) throws OperationTaskCancelledException {
        if (context != null) {
            context.throwIfCancelled();
        }
    }



    @FunctionalInterface

    private interface ProbeAction {

        void run(int index);

    }

}


