package com.moli.user.center.server.operation.service.impl;

import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.vo.OperationDeployStatusVo;
import com.moli.user.center.common.domain.vo.OperationHealthProbeResultVo;
import com.moli.user.center.server.operation.deploy.OperationDeployServiceKeys;
import com.moli.user.center.server.operation.health.OperationTcpProbe;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.service.OperationDeployService;
import com.moli.user.center.server.operation.service.OperationHealthProbeService;
import com.moli.user.center.server.operation.service.OperationProjectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OperationHealthProbeServiceImpl implements OperationHealthProbeService {

    @Resource
    private OperationServerMapper operationServerMapper;
    @Resource
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;
    @Resource
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Resource
    private OperationDeployService operationDeployService;
    @Resource
    private OperationProjectService operationProjectService;

    @Override
    public OperationHealthProbeResultVo probeAll() {
        OperationHealthProbeResultVo result = new OperationHealthProbeResultVo();
        List<OperationServerInfo> servers = operationServerMapper.selectList(null);
        for (OperationServerInfo server : servers) {
            int status = OperationTcpProbe.probe(server.getIp(), server.getPort());
            server.setStatus(status);
            server.setLastCheckTime(new Date());
            operationServerMapper.updateById(server);
            result.setServersProbed(result.getServersProbed() + 1);
        }

        List<OperationComponentDeployInfo> components = operationComponentDeployInfoMapper.selectList(null);
        for (OperationComponentDeployInfo component : components) {
            int status = OperationTcpProbe.probe(component.getServerIp(), component.getPort());
            component.setStatus(status);
            component.setLastCheckTime(new Date());
            operationComponentDeployInfoMapper.updateById(component);
            result.setComponentsProbed(result.getComponentsProbed() + 1);
        }

        List<OperationProjectDeployInfo> projects = operationProjectDeployInfoMapper.selectList(null);
        for (OperationProjectDeployInfo project : projects) {
            if (project.getServerId() == null && StringUtils.isNotBlank(project.getServerIp())) {
                operationProjectService.syncServerIdFromIp(project);
                if (project.getServerId() != null) {
                    operationProjectDeployInfoMapper.updateById(project);
                    result.setServerIdsSynced(result.getServerIdsSynced() + 1);
                }
            }
        }

        Map<String, Boolean> runningByServiceKey = new HashMap<>();
        for (OperationProjectDeployInfo project : projects) {
            String serviceKey = OperationDeployServiceKeys.resolve(project.getProjectName());
            if (serviceKey == null || runningByServiceKey.containsKey(serviceKey)) {
                continue;
            }
            try {
                OperationDeployStatusVo statusVo = operationDeployService.status(serviceKey);
                runningByServiceKey.put(serviceKey, Boolean.TRUE.equals(statusVo.getRunning()));
                result.setDeployStatusesSynced(result.getDeployStatusesSynced() + 1);
            } catch (Exception ex) {
                log.warn("deploy status sync failed for {}: {}", serviceKey, ex.getMessage());
                runningByServiceKey.put(serviceKey, null);
            }
        }

        Date now = new Date();
        for (OperationProjectDeployInfo project : projects) {
            String serviceKey = OperationDeployServiceKeys.resolve(project.getProjectName());
            if (serviceKey == null || !runningByServiceKey.containsKey(serviceKey)) {
                continue;
            }
            project.setDeployRunning(runningByServiceKey.get(serviceKey));
            project.setLastDeployCheckTime(now);
            operationProjectDeployInfoMapper.updateById(project);
        }

        return result;
    }
}
