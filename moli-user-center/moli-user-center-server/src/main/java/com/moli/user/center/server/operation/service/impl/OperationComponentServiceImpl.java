package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.dto.operation.OperationComponentSaveRequest;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.vo.OperationComponentVo;
import com.moli.user.center.common.domain.vo.OperationSecretRevealVo;
import com.moli.user.center.server.operation.audit.OperationPortMatrixPortCheck;
import com.moli.user.center.server.operation.audit.OperationPortMatrixProvider;
import com.moli.user.center.server.operation.health.OperationHealthStatus;
import com.moli.user.center.server.operation.health.OperationTcpProbe;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.service.OperationComponentLinkService;
import com.moli.user.center.server.operation.service.OperationComponentService;
import com.moli.user.center.server.operation.support.OperationCrudSupport;
import com.moli.user.center.server.operation.support.OperationRelationQuerySupport;
import com.moli.user.center.server.operation.support.OperationSaveRequestMapper;
import com.moli.user.center.server.operation.support.OperationSecretCrudSupport;
import com.moli.user.center.server.operation.support.OperationServerBindingSupport;
import com.moli.user.center.server.operation.support.OperationServerCascadeSupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OperationComponentServiceImpl implements OperationComponentService {

    @Resource
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;
    @Resource
    private OperationCrudSupport crudSupport;
    @Resource
    private OperationSecretCrudSupport secretCrudSupport;
    @Resource
    private OperationServerBindingSupport serverBindingSupport;
    @Resource
    private OperationServerCascadeSupport serverCascadeSupport;
    @Resource
    private OperationPortMatrixProvider portMatrixProvider;
    @Resource
    private OperationComponentLinkService operationComponentLinkService;
    @Resource
    private OperationRelationQuerySupport relationQuerySupport;

    @Override
    public PageRes<OperationComponentVo> list(OperationComponentDeployInfo query) {
        LambdaQueryWrapper<OperationComponentDeployInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getComponentName())) {
            wrapper.like(OperationComponentDeployInfo::getComponentName, query.getComponentName());
        }
        if (StringUtils.isNotBlank(query.getServerIp())) {
            wrapper.like(OperationComponentDeployInfo::getServerIp, query.getServerIp());
        }
        if (query.getServerId() != null) {
            relationQuerySupport.applyComponentServerFilter(wrapper, query.getServerId());
        }
        if (query.getProjectId() != null) {
            relationQuerySupport.applyComponentProjectFilter(wrapper, query.getProjectId());
        }
        if (query.getEnvironment() != null) {
            wrapper.eq(OperationComponentDeployInfo::getEnvironment, query.getEnvironment());
        }
        wrapper.orderByDesc(OperationComponentDeployInfo::getCreateTime);
        PageRes<OperationComponentVo> page = crudSupport.selectPage(operationComponentDeployInfoMapper, wrapper,
                query.getPageNum(), query.getPageSize(), this::toVo);
        fillRelationCounts(page.getList());
        return page;
    }

    @Override
    public OperationComponentVo getById(Long id) {
        return toVo(requireRow(id));
    }

    @Override
    public void create(OperationComponentSaveRequest request) {
        applyPrimaryServerFromList(request);
        OperationComponentDeployInfo row = OperationSaveRequestMapper.toEntity(request);
        serverBindingSupport.bindComponent(row);
        row.setPassword(secretCrudSupport.encryptOnSave(request.getPassword()));
        if (row.getStatus() == null) {
            row.setStatus(OperationHealthStatus.UNKNOWN);
        }
        operationComponentDeployInfoMapper.insert(row);
        operationComponentLinkService.syncLinks(row.getId(), request.getServerIds(), row.getServerId());
    }

    @Override
    public void update(OperationComponentSaveRequest request) {
        crudSupport.assertUpdateId(request.getId());
        OperationComponentDeployInfo existing = requireRow(request.getId());
        applyPrimaryServerFromList(request);
        OperationComponentDeployInfo row = OperationSaveRequestMapper.toEntity(request);
        serverBindingSupport.bindComponent(row);
        row.setPassword(secretCrudSupport.mergeOnUpdate(request.getPassword(), existing.getPassword()));
        row.setStatus(existing.getStatus());
        row.setLastCheckTime(existing.getLastCheckTime());
        operationComponentDeployInfoMapper.updateById(row);
        operationComponentLinkService.syncLinks(row.getId(), request.getServerIds(), row.getServerId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(Long[] ids) {
        crudSupport.deleteEach(ids,
                serverCascadeSupport::onDeleteComponent,
                operationComponentDeployInfoMapper::deleteById);
    }

    @Override
    public OperationSecretRevealVo revealPassword(Long id) {
        return secretCrudSupport.reveal(requireRow(id).getPassword());
    }

    @Override
    public OperationComponentVo checkHealth(Long id) {
        OperationComponentDeployInfo row = requireRow(id);
        int status = OperationTcpProbe.probe(row.getServerIp(), row.getPort());
        row.setStatus(status);
        row.setLastCheckTime(new Date());
        operationComponentDeployInfoMapper.updateById(row);
        return toVo(row);
    }

    private OperationComponentDeployInfo requireRow(Long id) {
        return crudSupport.requireRow(operationComponentDeployInfoMapper, id, "运维组件");
    }

    private OperationComponentVo toVo(OperationComponentDeployInfo row) {
        OperationComponentVo vo = new OperationComponentVo();
        BeanUtils.copyProperties(row, vo);
        vo.setPasswordConfigured(secretCrudSupport.passwordConfigured(row.getPassword()));
        vo.setPasswordMask(secretCrudSupport.passwordMask(row.getPassword()));
        vo.setStatus(row.getStatus());
        vo.setLastCheckTime(row.getLastCheckTime());
        vo.setServerIds(relationQuerySupport.resolveServerIdsForComponent(row.getId(), row.getServerId()));
        OperationPortMatrixPortCheck portCheck = portMatrixProvider.check(row.getComponentName(), row.getPort());
        vo.setExpectedPort(portCheck.expectedPort);
        vo.setPortMatchStatus(portCheck.status);
        return vo;
    }

    private void fillRelationCounts(List<OperationComponentVo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> ids = list.stream().map(OperationComponentVo::getId).collect(Collectors.toList());
        Map<Long, Long> primaryByComponent = new HashMap<>();
        for (OperationComponentVo vo : list) {
            primaryByComponent.put(vo.getId(), vo.getServerId());
        }
        Map<Long, Integer> serverCounts = relationQuerySupport.countServersByComponentIds(ids, primaryByComponent);
        Map<Long, Integer> projectCounts = relationQuerySupport.countProjectsByComponentIds(ids);
        for (OperationComponentVo vo : list) {
            vo.setServerCount(serverCounts.getOrDefault(vo.getId(), 0));
            vo.setProjectCount(projectCounts.getOrDefault(vo.getId(), 0));
        }
    }

    private void applyPrimaryServerFromList(OperationComponentSaveRequest request) {
        if (request == null) {
            return;
        }
        if (request.getServerIds() != null && !request.getServerIds().isEmpty()) {
            request.setServerId(request.getServerIds().get(0));
        }
    }
}
