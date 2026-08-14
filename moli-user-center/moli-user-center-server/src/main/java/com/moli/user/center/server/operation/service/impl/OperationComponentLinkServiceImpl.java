package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.core.IdGenerator;
import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerComponent;
import com.moli.user.center.common.domain.vo.OperationComponentLinksVo;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerComponentLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.service.OperationComponentLinkService;
import com.moli.user.center.server.operation.support.OperationServerBindingSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class OperationComponentLinkServiceImpl implements OperationComponentLinkService {

    @Resource
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;
    @Resource
    private OperationServerMapper operationServerMapper;
    @Resource
    private OperationServerLinkMapper operationServerLinkMapper;
    @Resource
    private OperationServerComponentLinkMapper operationServerComponentLinkMapper;
    @Resource
    private OperationServerBindingSupport serverBindingSupport;

    @Override
    public OperationComponentLinksVo getLinks(Long componentId) {
        requireComponent(componentId);
        OperationComponentLinksVo vo = new OperationComponentLinksVo();
        vo.setComponentId(componentId);
        vo.setServerIds(operationServerLinkMapper.selectServerIdsByComponentId(componentId));
        return vo;
    }

    @Override
    public List<OperationComponentLinksVo> getLinksBatch(List<Long> componentIds) {
        List<OperationComponentLinksVo> items = new ArrayList<>();
        if (componentIds == null) {
            return items;
        }
        for (Long componentId : componentIds) {
            if (componentId != null) {
                items.add(getLinks(componentId));
            }
        }
        return items;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLinks(Long componentId, OperationComponentLinksVo links) {
        OperationComponentDeployInfo component = requireComponent(componentId);
        List<Long> serverIds = distinctIds(links == null ? null : links.getServerIds());
        syncLinks(componentId, serverIds, serverIds.isEmpty() ? null : serverIds.get(0));
        syncPrimaryServer(component, serverIds);
        operationComponentDeployInfoMapper.updateById(component);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncLinks(Long componentId, List<Long> serverIds, Long primaryServerId) {
        requireComponent(componentId);
        List<Long> resolved = resolveServerIds(serverIds, primaryServerId);
        validateServerIds(resolved);

        LambdaQueryWrapper<OperationServerComponent> delete = new LambdaQueryWrapper<>();
        delete.eq(OperationServerComponent::getComponentId, componentId);
        operationServerComponentLinkMapper.delete(delete);

        for (Long serverId : resolved) {
            OperationServerComponent row = new OperationServerComponent();
            row.setId(IdGenerator.getId());
            row.setServerId(serverId);
            row.setComponentId(componentId);
            operationServerComponentLinkMapper.insert(row);
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

    private void syncPrimaryServer(OperationComponentDeployInfo component, List<Long> serverIds) {
        if (serverIds == null || serverIds.isEmpty()) {
            component.setServerId(null);
            component.setServerIp(null);
            return;
        }
        component.setServerId(serverIds.get(0));
        serverBindingSupport.bindComponent(component);
    }

    private OperationComponentDeployInfo requireComponent(Long componentId) {
        OperationComponentDeployInfo component = operationComponentDeployInfoMapper.selectById(componentId);
        if (component == null) {
            throw new BaseException("组件不存在");
        }
        return component;
    }
}
