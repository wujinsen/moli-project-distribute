package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.user.center.common.domain.dto.operation.OperationServerTagsSupport;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.entity.OperationTask;
import com.moli.user.center.common.domain.vo.OperationRelationComponentItemVo;
import com.moli.user.center.common.domain.vo.OperationRelationEntityVo;
import com.moli.user.center.common.domain.vo.OperationRelationProjectItemVo;
import com.moli.user.center.common.domain.vo.OperationRelationServerItemVo;
import com.moli.user.center.common.domain.vo.OperationRelationTaskItemVo;
import com.moli.user.center.common.domain.vo.OperationRelationsVo;
import com.moli.user.center.server.operation.audit.OperationPortMatrixPortCheck;
import com.moli.user.center.server.operation.audit.OperationPortMatrixProvider;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.mapper.OperationTaskMapper;
import com.moli.user.center.server.operation.service.OperationRelationService;
import com.moli.user.center.server.operation.support.OperationBizException;
import com.moli.user.center.server.operation.support.OperationRelationQuerySupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class OperationRelationServiceImpl implements OperationRelationService {

    @Resource
    private OperationRelationQuerySupport relationQuerySupport;
    @Resource
    private OperationServerMapper operationServerMapper;
    @Resource
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Resource
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;
    @Resource
    private OperationPortMatrixProvider portMatrixProvider;
    @Resource
    private OperationTaskMapper operationTaskMapper;

    @Override
    public OperationRelationsVo getRelations(String entityType, Long id) {
        String type = normalizeType(entityType);
        OperationRelationsVo vo = new OperationRelationsVo();
        vo.setEntityType(type);
        switch (type) {
            case "server":
                fillForServer(vo, id);
                break;
            case "project":
                fillForProject(vo, id);
                break;
            case "component":
                fillForComponent(vo, id);
                break;
            default:
                throw OperationBizException.params("不支持的实体类型: " + entityType);
        }
        return vo;
    }

    private void fillForServer(OperationRelationsVo vo, Long serverId) {
        OperationServerInfo server = requireServer(serverId);
        vo.setEntity(toEntity("server", server.getId(), server.getServerName(), server.getEnvironment()));

        List<Long> projectIds = relationQuerySupport.resolveProjectIdsForServer(serverId);
        vo.setProjects(loadProjects(projectIds));

        List<Long> componentIds = relationQuerySupport.resolveComponentIdsForServer(serverId);
        vo.setComponents(loadComponents(componentIds));

        vo.setRecentTasks(loadRecentTasks(serverId, null));
    }

    private void fillForProject(OperationRelationsVo vo, Long projectId) {
        OperationProjectDeployInfo project = requireProject(projectId);
        vo.setEntity(toEntity("project", project.getId(), project.getProjectName(), project.getEnvironment()));

        List<Long> serverIds = relationQuerySupport.resolveServerIdsForProject(projectId, project.getServerId());
        vo.setServers(loadServers(serverIds, project.getServerId()));

        List<Long> componentIds = relationQuerySupport.resolveComponentIdsForProject(projectId);
        vo.setComponents(loadComponents(componentIds));

        vo.setRecentTasks(loadRecentTasks(null, projectId));
    }

    private void fillForComponent(OperationRelationsVo vo, Long componentId) {
        OperationComponentDeployInfo component = requireComponent(componentId);
        vo.setEntity(toEntity("component", component.getId(), component.getComponentName(), component.getEnvironment()));

        List<Long> serverIds = relationQuerySupport.resolveServerIdsForComponent(componentId, component.getServerId());
        vo.setServers(loadServers(serverIds, component.getServerId()));

        List<Long> projectIds = relationQuerySupport.resolveProjectIdsForComponent(componentId);
        vo.setProjects(loadProjects(projectIds));

        vo.setRecentTasks(loadRecentTasks(component.getServerId(), null));
    }

    private List<OperationRelationServerItemVo> loadServers(List<Long> serverIds, Long primaryServerId) {
        if (serverIds == null || serverIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<OperationRelationServerItemVo> items = new ArrayList<>();
        for (OperationServerInfo row : operationServerMapper.selectBatchIds(serverIds)) {
            OperationRelationServerItemVo item = new OperationRelationServerItemVo();
            item.setId(row.getId());
            item.setServerName(row.getServerName());
            item.setIp(row.getIp());
            item.setInnerIp(row.getInnerIp());
            item.setEnvironment(row.getEnvironment());
            item.setServerRole(row.getServerRole());
            item.setTags(OperationServerTagsSupport.parse(row.getTags()));
            item.setStatus(row.getStatus());
            item.setPrimary(primaryServerId != null && primaryServerId.equals(row.getId()));
            items.add(item);
        }
        return items;
    }

    private List<OperationRelationProjectItemVo> loadProjects(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<OperationRelationProjectItemVo> items = new ArrayList<>();
        for (OperationProjectDeployInfo row : operationProjectDeployInfoMapper.selectBatchIds(projectIds)) {
            OperationRelationProjectItemVo item = new OperationRelationProjectItemVo();
            item.setId(row.getId());
            item.setProjectName(row.getProjectName());
            item.setPort(row.getPort());
            item.setEnvironment(row.getEnvironment());
            item.setDeployRunning(row.getDeployRunning());
            OperationPortMatrixPortCheck check = portMatrixProvider.check(row.getProjectName(), row.getPort());
            item.setPortMatchStatus(check.status);
            items.add(item);
        }
        return items;
    }

    private List<OperationRelationComponentItemVo> loadComponents(List<Long> componentIds) {
        if (componentIds == null || componentIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<OperationRelationComponentItemVo> items = new ArrayList<>();
        for (OperationComponentDeployInfo row : operationComponentDeployInfoMapper.selectBatchIds(componentIds)) {
            OperationRelationComponentItemVo item = new OperationRelationComponentItemVo();
            item.setId(row.getId());
            item.setComponentName(row.getComponentName());
            item.setPort(row.getPort());
            item.setVersion(row.getVersion());
            item.setEnvironment(row.getEnvironment());
            item.setStatus(row.getStatus());
            OperationPortMatrixPortCheck check = portMatrixProvider.check(row.getComponentName(), row.getPort());
            item.setPortMatchStatus(check.status);
            items.add(item);
        }
        return items;
    }

    private List<OperationRelationTaskItemVo> loadRecentTasks(Long serverId, Long projectId) {
        if (serverId == null && projectId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<OperationTask> wrapper = new LambdaQueryWrapper<>();
        if (serverId != null) {
            wrapper.eq(OperationTask::getServerId, serverId);
        }
        if (projectId != null) {
            wrapper.eq(OperationTask::getProjectId, projectId);
        }
        wrapper.select(OperationTask.class, f -> !"task_log".equals(f.getColumn()));
        wrapper.orderByDesc(OperationTask::getCreateTime);
        wrapper.last("LIMIT 5");
        List<OperationRelationTaskItemVo> items = new ArrayList<>();
        for (OperationTask task : operationTaskMapper.selectList(wrapper)) {
            OperationRelationTaskItemVo item = new OperationRelationTaskItemVo();
            item.setId(task.getId());
            item.setTaskType(task.getTaskType());
            item.setAction(task.getAction());
            item.setStatus(task.getStatus());
            item.setCreateTime(task.getCreateTime());
            items.add(item);
        }
        return items;
    }

    private static OperationRelationEntityVo toEntity(String type, Long id, String name, Integer environment) {
        OperationRelationEntityVo entity = new OperationRelationEntityVo();
        entity.setEntityType(type);
        entity.setId(id);
        entity.setName(name);
        entity.setEnvironment(environment);
        return entity;
    }

    private static String normalizeType(String entityType) {
        if (StringUtils.isBlank(entityType)) {
            throw OperationBizException.params("实体类型不能为空");
        }
        return entityType.trim().toLowerCase();
    }

    private OperationServerInfo requireServer(Long id) {
        OperationServerInfo row = operationServerMapper.selectById(id);
        if (row == null) {
            throw OperationBizException.notFound("服务器", id);
        }
        return row;
    }

    private OperationProjectDeployInfo requireProject(Long id) {
        OperationProjectDeployInfo row = operationProjectDeployInfoMapper.selectById(id);
        if (row == null) {
            throw OperationBizException.notFound("项目", id);
        }
        return row;
    }

    private OperationComponentDeployInfo requireComponent(Long id) {
        OperationComponentDeployInfo row = operationComponentDeployInfoMapper.selectById(id);
        if (row == null) {
            throw OperationBizException.notFound("组件", id);
        }
        return row;
    }
}
