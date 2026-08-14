package com.moli.user.center.server.operation.service.impl;

import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.vo.OperationRelationReconcileVo;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerLinkMapper;
import com.moli.user.center.server.operation.service.OperationRelationRepairService;
import com.moli.user.center.server.operation.support.OperationServerBindingSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class OperationRelationRepairServiceImpl implements OperationRelationRepairService {

    @Resource
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Resource
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;
    @Resource
    private OperationServerLinkMapper operationServerLinkMapper;
    @Resource
    private OperationServerBindingSupport serverBindingSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OperationRelationReconcileVo reconcilePrimaryServers() {
        OperationRelationReconcileVo result = new OperationRelationReconcileVo();
        reconcileProjects(result);
        reconcileComponents(result);
        return result;
    }

    private void reconcileProjects(OperationRelationReconcileVo result) {
        List<Long> projectIds = safeList(operationServerLinkMapper.selectDistinctProjectIdsWithLinks());
        for (Long projectId : projectIds) {
            OperationProjectDeployInfo project = operationProjectDeployInfoMapper.selectById(projectId);
            if (project == null) {
                continue;
            }
            List<Long> linked = safeList(operationServerLinkMapper.selectServerIdsByProjectId(projectId));
            if (linked.isEmpty()) {
                continue;
            }
            Long expectedServerId = linked.get(0);
            if (Objects.equals(project.getServerId(), expectedServerId)) {
                continue;
            }
            project.setServerId(expectedServerId);
            serverBindingSupport.bindProject(project);
            operationProjectDeployInfoMapper.updateById(project);
            result.setProjectsFixed(result.getProjectsFixed() + 1);
            result.getDetails().add("project:" + projectId + " serverId->" + expectedServerId);
        }
    }

    private void reconcileComponents(OperationRelationReconcileVo result) {
        List<Long> componentIds = safeList(operationServerLinkMapper.selectDistinctComponentIdsWithLinks());
        for (Long componentId : componentIds) {
            OperationComponentDeployInfo component = operationComponentDeployInfoMapper.selectById(componentId);
            if (component == null) {
                continue;
            }
            List<Long> linked = safeList(operationServerLinkMapper.selectServerIdsByComponentId(componentId));
            if (linked.isEmpty()) {
                continue;
            }
            Long expectedServerId = linked.get(0);
            if (Objects.equals(component.getServerId(), expectedServerId)) {
                continue;
            }
            component.setServerId(expectedServerId);
            serverBindingSupport.bindComponent(component);
            operationComponentDeployInfoMapper.updateById(component);
            result.setComponentsFixed(result.getComponentsFixed() + 1);
            result.getDetails().add("component:" + componentId + " serverId->" + expectedServerId);
        }
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
}
