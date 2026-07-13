package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.core.IdGenerator;
import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.entity.OperationServerProject;
import com.moli.user.center.common.domain.vo.OperationProjectLinksVo;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.mapper.OperationServerProjectLinkMapper;
import com.moli.user.center.server.operation.service.OperationProjectLinkService;
import com.moli.user.center.server.operation.support.OperationServerBindingSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class OperationProjectLinkServiceImpl implements OperationProjectLinkService {

    @Resource
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Resource
    private OperationServerMapper operationServerMapper;
    @Resource
    private OperationServerLinkMapper operationServerLinkMapper;
    @Resource
    private OperationServerProjectLinkMapper operationServerProjectLinkMapper;
    @Resource
    private OperationServerBindingSupport serverBindingSupport;

    @Override
    public OperationProjectLinksVo getLinks(Long projectId) {
        OperationProjectDeployInfo project = requireProject(projectId);
        OperationProjectLinksVo vo = new OperationProjectLinksVo();
        vo.setProjectId(projectId);
        List<Long> linked = operationServerLinkMapper.selectServerIdsByProjectId(projectId);
        if (linked == null || linked.isEmpty()) {
            if (project.getServerId() != null) {
                linked = Collections.singletonList(project.getServerId());
            }
        }
        vo.setServerIds(linked == null ? new ArrayList<>() : linked);
        return vo;
    }

    @Override
    public List<OperationProjectLinksVo> getLinksBatch(List<Long> projectIds) {
        List<OperationProjectLinksVo> items = new ArrayList<>();
        if (projectIds == null) {
            return items;
        }
        for (Long projectId : projectIds) {
            if (projectId != null) {
                items.add(getLinks(projectId));
            }
        }
        return items;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLinks(Long projectId, OperationProjectLinksVo links) {
        OperationProjectDeployInfo project = requireProject(projectId);
        List<Long> serverIds = distinctIds(links == null ? null : links.getServerIds());
        syncLinks(projectId, serverIds, serverIds.isEmpty() ? null : serverIds.get(0));
        syncPrimaryServer(project, serverIds);
        operationProjectDeployInfoMapper.updateById(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncLinks(Long projectId, List<Long> serverIds, Long primaryServerId) {
        requireProject(projectId);
        List<Long> resolved = resolveServerIds(serverIds, primaryServerId);
        validateServerIds(resolved);

        LambdaQueryWrapper<OperationServerProject> delete = new LambdaQueryWrapper<>();
        delete.eq(OperationServerProject::getProjectId, projectId);
        operationServerProjectLinkMapper.delete(delete);

        for (Long serverId : resolved) {
            OperationServerProject row = new OperationServerProject();
            row.setId(IdGenerator.getId());
            row.setServerId(serverId);
            row.setProjectId(projectId);
            operationServerProjectLinkMapper.insert(row);
        }
    }

    private List<Long> resolveServerIds(List<Long> serverIds, Long primaryServerId) {
        Set<Long> ordered = new LinkedHashSet<>();
        if (primaryServerId != null) {
            ordered.add(primaryServerId);
        }
        if (serverIds != null) {
            for (Long id : serverIds) {
                if (id != null) {
                    ordered.add(id);
                }
            }
        }
        return new ArrayList<>(ordered);
    }

    private List<Long> distinctIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> ordered = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id != null) {
                ordered.add(id);
            }
        }
        return new ArrayList<>(ordered);
    }

    private void validateServerIds(List<Long> serverIds) {
        for (Long serverId : serverIds) {
            if (operationServerMapper.selectById(serverId) == null) {
                throw new BaseException("服务器不存在: " + serverId);
            }
        }
    }

    private void syncPrimaryServer(OperationProjectDeployInfo project, List<Long> serverIds) {
        if (serverIds == null || serverIds.isEmpty()) {
            project.setServerId(null);
            project.setServerIp(null);
            project.setInnerIp(null);
            return;
        }
        project.setServerId(serverIds.get(0));
        serverBindingSupport.bindProject(project);
    }

    private OperationProjectDeployInfo requireProject(Long projectId) {
        OperationProjectDeployInfo project = operationProjectDeployInfoMapper.selectById(projectId);
        if (project == null) {
            throw new BaseException("项目不存在");
        }
        return project;
    }
}
