package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.core.IdGenerator;
import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerComponent;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.entity.OperationServerProject;
import com.moli.user.center.common.domain.vo.OperationServerLinksVo;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerComponentLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.mapper.OperationServerProjectLinkMapper;
import com.moli.user.center.server.operation.service.OperationServerLinkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class OperationServerLinkServiceImpl implements OperationServerLinkService {

    @Resource
    private OperationServerMapper operationServerMapper;
    @Resource
    private OperationServerLinkMapper operationServerLinkMapper;
    @Resource
    private OperationServerProjectLinkMapper operationServerProjectLinkMapper;
    @Resource
    private OperationServerComponentLinkMapper operationServerComponentLinkMapper;
    @Resource
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Resource
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;

    @Override
    public OperationServerLinksVo getLinks(Long serverId) {
        requireServer(serverId);
        OperationServerLinksVo vo = new OperationServerLinksVo();
        vo.setServerId(serverId);
        vo.setProjectIds(operationServerLinkMapper.selectProjectIdsByServerId(serverId));
        vo.setComponentIds(operationServerLinkMapper.selectComponentIdsByServerId(serverId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLinks(Long serverId, OperationServerLinksVo links) {
        requireServer(serverId);
        List<Long> projectIds = distinctIds(links == null ? null : links.getProjectIds());
        List<Long> componentIds = distinctIds(links == null ? null : links.getComponentIds());
        validateProjectIds(projectIds);
        validateComponentIds(componentIds);

        LambdaQueryWrapper<OperationServerProject> deleteProjects = new LambdaQueryWrapper<>();
        deleteProjects.eq(OperationServerProject::getServerId, serverId);
        operationServerProjectLinkMapper.delete(deleteProjects);

        LambdaQueryWrapper<OperationServerComponent> deleteComponents = new LambdaQueryWrapper<>();
        deleteComponents.eq(OperationServerComponent::getServerId, serverId);
        operationServerComponentLinkMapper.delete(deleteComponents);

        for (Long projectId : projectIds) {
            OperationServerProject row = new OperationServerProject();
            row.setId(IdGenerator.getId());
            row.setServerId(serverId);
            row.setProjectId(projectId);
            operationServerProjectLinkMapper.insert(row);
        }
        for (Long componentId : componentIds) {
            OperationServerComponent row = new OperationServerComponent();
            row.setId(IdGenerator.getId());
            row.setServerId(serverId);
            row.setComponentId(componentId);
            operationServerComponentLinkMapper.insert(row);
        }
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

    private void validateProjectIds(List<Long> projectIds) {
        for (Long projectId : projectIds) {
            if (operationProjectDeployInfoMapper.selectById(projectId) == null) {
                throw new BaseException("项目不存在: " + projectId);
            }
        }
    }

    private void validateComponentIds(List<Long> componentIds) {
        for (Long componentId : componentIds) {
            if (operationComponentDeployInfoMapper.selectById(componentId) == null) {
                throw new BaseException("组件不存在: " + componentId);
            }
        }
    }

    private OperationServerInfo requireServer(Long serverId) {
        OperationServerInfo server = operationServerMapper.selectById(serverId);
        if (server == null) {
            throw new BaseException("服务器不存在");
        }
        return server;
    }
}
