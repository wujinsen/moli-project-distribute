package com.moli.user.center.server.operation.service.impl;

import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.vo.OperationDeployStatusVo;
import com.moli.user.center.server.operation.config.OperationDeployProperties;
import com.moli.user.center.server.operation.config.OperationDeployStatusSyncMode;
import com.moli.user.center.server.operation.deploy.OperationDeployServiceRegistry;
import com.moli.user.center.server.operation.service.OperationDeployService;
import com.moli.user.center.server.operation.service.OperationDeployStatusSyncService;
import com.moli.user.center.server.operation.service.OperationRemoteDeployService;
import com.moli.user.center.server.operation.support.OperationHostEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Locale;

@Service
@Slf4j
public class OperationDeployStatusSyncServiceImpl implements OperationDeployStatusSyncService {

    @Resource
    private OperationDeployProperties deployProperties;
    @Resource
    private OperationDeployService operationDeployService;
    @Resource
    private OperationRemoteDeployService operationRemoteDeployService;
    @Resource
    private OperationDeployServiceRegistry deployServiceRegistry;
    @Resource
    private OperationHostEnvironment hostEnvironment;

    @Override
    public boolean syncProject(OperationProjectDeployInfo project) {
        String serviceKey = deployServiceRegistry.resolveProjectName(project.getProjectName());
        if (serviceKey == null) {
            return false;
        }
        String mode = OperationDeployStatusSyncMode.normalize(deployProperties.getStatusSyncMode());
        if (OperationDeployStatusSyncMode.OFF.equals(mode)) {
            return false;
        }

        OperationDeployStatusVo statusVo = resolveStatus(project, serviceKey, mode);
        if (statusVo == null) {
            return false;
        }
        if (!Boolean.TRUE.equals(statusVo.getAvailable())) {
            log.warn("deploy status unavailable for project {} (serverId={}, serviceKey={}): {}",
                    project.getProjectName(), project.getServerId(), serviceKey, statusVo.getMessage());
            return false;
        }

        project.setDeployRunning(statusVo.getRunning());
        project.setLastDeployCheckTime(new Date());
        return true;
    }

    private OperationDeployStatusVo resolveStatus(OperationProjectDeployInfo project,
                                                  String serviceKey, String mode) {
        if (project.getServerId() != null && !OperationDeployStatusSyncMode.LOCAL.equals(mode)) {
            return operationRemoteDeployService.executeRemoteReadOnly(
                    project.getServerId(), serviceKey, "status", null);
        }
        if (hostEnvironment.isLocalLinux()) {
            return operationDeployService.status(serviceKey);
        }
        return null;
    }
}
