package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.core.IdGenerator;
import com.moli.common.exception.BaseException;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectComponent;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.vo.OperationProjectComponentLinksVo;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectComponentLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.service.OperationProjectComponentLinkService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class OperationProjectComponentLinkServiceImpl implements OperationProjectComponentLinkService {

    @Resource
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Resource
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;
    @Resource
    private OperationProjectComponentLinkMapper operationProjectComponentLinkMapper;

    @Override
    public OperationProjectComponentLinksVo getLinks(Long projectId) {
        requireProject(projectId);
        OperationProjectComponentLinksVo vo = new OperationProjectComponentLinksVo();
        vo.setProjectId(projectId);
        vo.setComponentIds(operationProjectComponentLinkMapper.selectComponentIdsByProjectId(projectId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLinks(Long projectId, OperationProjectComponentLinksVo links) {
        requireProject(projectId);
        syncLinks(projectId, distinctIds(links == null ? null : links.getComponentIds()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncLinks(Long projectId, List<Long> componentIds) {
        requireProject(projectId);
        List<Long> resolved = distinctIds(componentIds);
        validateComponentIds(resolved);

        LambdaQueryWrapper<OperationProjectComponent> delete = new LambdaQueryWrapper<>();
        delete.eq(OperationProjectComponent::getProjectId, projectId);
        operationProjectComponentLinkMapper.delete(delete);

        for (Long componentId : resolved) {
            OperationProjectComponent row = new OperationProjectComponent();
            row.setId(IdGenerator.getId());
            row.setProjectId(projectId);
            row.setComponentId(componentId);
            operationProjectComponentLinkMapper.insert(row);
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

    private void validateComponentIds(List<Long> componentIds) {
        for (Long componentId : componentIds) {
            if (operationComponentDeployInfoMapper.selectById(componentId) == null) {
                throw new BaseException("组件不存在: " + componentId);
            }
        }
    }

    private OperationProjectDeployInfo requireProject(Long projectId) {
        OperationProjectDeployInfo project = operationProjectDeployInfoMapper.selectById(projectId);
        if (project == null) {
            throw new BaseException("项目不存在");
        }
        return project;
    }
}
