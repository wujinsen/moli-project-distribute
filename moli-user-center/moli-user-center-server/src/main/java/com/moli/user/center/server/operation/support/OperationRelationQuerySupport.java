package com.moli.user.center.server.operation.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectComponent;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectComponentLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerLinkMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 服务器 / 项目 / 组件 N:N 关系读取（含主 server_id 回退），供列表计数、关系 API、拓扑图共用。
 */
@Component
public class OperationRelationQuerySupport {

    @Resource
    private OperationServerLinkMapper operationServerLinkMapper;
    @Resource
    private OperationProjectComponentLinkMapper operationProjectComponentLinkMapper;
    @Resource
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Resource
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;

    public List<Long> resolveServerIdsForProject(Long projectId, Long primaryServerId) {
        Set<Long> ids = new LinkedHashSet<>(safeList(operationServerLinkMapper.selectServerIdsByProjectId(projectId)));
        if (primaryServerId != null) {
            ids.add(primaryServerId);
        }
        return new ArrayList<>(ids);
    }

    public List<Long> resolveServerIdsForComponent(Long componentId, Long primaryServerId) {
        Set<Long> ids = new LinkedHashSet<>(safeList(operationServerLinkMapper.selectServerIdsByComponentId(componentId)));
        if (primaryServerId != null) {
            ids.add(primaryServerId);
        }
        return new ArrayList<>(ids);
    }

    public List<Long> resolveProjectIdsForServer(Long serverId) {
        Set<Long> ids = new LinkedHashSet<>(safeList(operationServerLinkMapper.selectProjectIdsByServerId(serverId)));
        LambdaQueryWrapper<OperationProjectDeployInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationProjectDeployInfo::getServerId, serverId);
        wrapper.select(OperationProjectDeployInfo::getId);
        operationProjectDeployInfoMapper.selectList(wrapper).forEach(row -> ids.add(row.getId()));
        return new ArrayList<>(ids);
    }

    public List<Long> resolveComponentIdsForServer(Long serverId) {
        Set<Long> ids = new LinkedHashSet<>(safeList(operationServerLinkMapper.selectComponentIdsByServerId(serverId)));
        LambdaQueryWrapper<OperationComponentDeployInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationComponentDeployInfo::getServerId, serverId);
        wrapper.select(OperationComponentDeployInfo::getId);
        operationComponentDeployInfoMapper.selectList(wrapper).forEach(row -> ids.add(row.getId()));
        return new ArrayList<>(ids);
    }

    public List<Long> resolveComponentIdsForProject(Long projectId) {
        return safeList(operationProjectComponentLinkMapper.selectComponentIdsByProjectId(projectId));
    }

    public List<Long> resolveProjectIdsForComponent(Long componentId) {
        return safeList(operationProjectComponentLinkMapper.selectProjectIdsByComponentId(componentId));
    }

    public Map<Long, Integer> countServersByProjectIds(Collection<Long> projectIds, Map<Long, Long> primaryServerByProject) {
        Map<Long, Integer> counts = initZeroCounts(projectIds);
        if (projectIds == null || projectIds.isEmpty()) {
            return counts;
        }
        for (Long projectId : projectIds) {
            counts.put(projectId, resolveServerIdsForProject(projectId,
                    primaryServerByProject == null ? null : primaryServerByProject.get(projectId)).size());
        }
        return counts;
    }

    public Map<Long, Integer> countComponentsByProjectIds(Collection<Long> projectIds) {
        Map<Long, Integer> counts = initZeroCounts(projectIds);
        if (projectIds == null || projectIds.isEmpty()) {
            return counts;
        }
        LambdaQueryWrapper<OperationProjectComponent> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(OperationProjectComponent::getProjectId, projectIds);
        for (OperationProjectComponent row : operationProjectComponentLinkMapper.selectList(wrapper)) {
            counts.merge(row.getProjectId(), 1, Integer::sum);
        }
        return counts;
    }

    public Map<Long, Integer> countServersByComponentIds(Collection<Long> componentIds, Map<Long, Long> primaryServerByComponent) {
        Map<Long, Integer> counts = initZeroCounts(componentIds);
        if (componentIds == null || componentIds.isEmpty()) {
            return counts;
        }
        for (Long componentId : componentIds) {
            counts.put(componentId, resolveServerIdsForComponent(componentId,
                    primaryServerByComponent == null ? null : primaryServerByComponent.get(componentId)).size());
        }
        return counts;
    }

    public Map<Long, Integer> countProjectsByComponentIds(Collection<Long> componentIds) {
        Map<Long, Integer> counts = initZeroCounts(componentIds);
        if (componentIds == null || componentIds.isEmpty()) {
            return counts;
        }
        LambdaQueryWrapper<OperationProjectComponent> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(OperationProjectComponent::getComponentId, componentIds);
        for (OperationProjectComponent row : operationProjectComponentLinkMapper.selectList(wrapper)) {
            counts.merge(row.getComponentId(), 1, Integer::sum);
        }
        return counts;
    }

    public Map<Long, Integer> countProjectsByServerIds(Collection<Long> serverIds) {
        Map<Long, Integer> counts = initZeroCounts(serverIds);
        if (serverIds == null || serverIds.isEmpty()) {
            return counts;
        }
        for (Long serverId : serverIds) {
            counts.put(serverId, resolveProjectIdsForServer(serverId).size());
        }
        return counts;
    }

    public Map<Long, Integer> countComponentsByServerIds(Collection<Long> serverIds) {
        Map<Long, Integer> counts = initZeroCounts(serverIds);
        if (serverIds == null || serverIds.isEmpty()) {
            return counts;
        }
        for (Long serverId : serverIds) {
            counts.put(serverId, resolveComponentIdsForServer(serverId).size());
        }
        return counts;
    }

    public void applyProjectServerFilter(LambdaQueryWrapper<OperationProjectDeployInfo> wrapper, Long serverId) {
        if (serverId == null) {
            return;
        }
        wrapper.and(w -> w.eq(OperationProjectDeployInfo::getServerId, serverId)
                .or()
                .inSql(OperationProjectDeployInfo::getId,
                        "SELECT project_id FROM operation_server_project WHERE server_id = " + serverId));
    }

    public void applyProjectComponentFilter(LambdaQueryWrapper<OperationProjectDeployInfo> wrapper, Long componentId) {
        if (componentId == null) {
            return;
        }
        wrapper.inSql(OperationProjectDeployInfo::getId,
                "SELECT project_id FROM operation_project_component WHERE component_id = " + componentId);
    }

    public void applyComponentServerFilter(LambdaQueryWrapper<OperationComponentDeployInfo> wrapper, Long serverId) {
        if (serverId == null) {
            return;
        }
        wrapper.and(w -> w.eq(OperationComponentDeployInfo::getServerId, serverId)
                .or()
                .inSql(OperationComponentDeployInfo::getId,
                        "SELECT component_id FROM operation_server_component WHERE server_id = " + serverId));
    }

    public void applyComponentProjectFilter(LambdaQueryWrapper<OperationComponentDeployInfo> wrapper, Long projectId) {
        if (projectId == null) {
            return;
        }
        wrapper.inSql(OperationComponentDeployInfo::getId,
                "SELECT component_id FROM operation_project_component WHERE project_id = " + projectId);
    }

    public void applyServerProjectFilter(LambdaQueryWrapper<OperationServerInfo> wrapper, Long projectId) {
        if (projectId == null) {
            return;
        }
        wrapper.and(w -> w.inSql(OperationServerInfo::getId,
                        "SELECT server_id FROM operation_server_project WHERE project_id = " + projectId)
                .or()
                .inSql(OperationServerInfo::getId,
                        "SELECT server_id FROM operation_project_deploy_info WHERE id = " + projectId + " AND server_id IS NOT NULL"));
    }

    public void applyServerComponentFilter(LambdaQueryWrapper<OperationServerInfo> wrapper, Long componentId) {
        if (componentId == null) {
            return;
        }
        wrapper.and(w -> w.inSql(OperationServerInfo::getId,
                        "SELECT server_id FROM operation_server_component WHERE component_id = " + componentId)
                .or()
                .inSql(OperationServerInfo::getId,
                        "SELECT server_id FROM operation_component_deploy_info WHERE id = " + componentId + " AND server_id IS NOT NULL"));
    }

    private static Map<Long, Integer> initZeroCounts(Collection<Long> ids) {
        Map<Long, Integer> counts = new HashMap<>();
        if (ids == null) {
            return counts;
        }
        for (Long id : ids) {
            counts.put(id, 0);
        }
        return counts;
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? Collections.emptyList() : list;
    }
}
