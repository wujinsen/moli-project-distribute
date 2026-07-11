package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.dto.operation.OperationProjectSaveRequest;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.vo.OperationProjectVo;
import com.moli.user.center.server.operation.audit.OperationPortMatrixPortCheck;
import com.moli.user.center.server.operation.audit.OperationPortMatrixProvider;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.service.OperationProjectService;
import com.moli.user.center.server.operation.support.OperationCrudSupport;
import com.moli.user.center.server.operation.support.OperationSaveRequestMapper;
import com.moli.user.center.server.operation.support.OperationServerBindingSupport;
import com.moli.user.center.server.operation.support.OperationServerCascadeSupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class OperationProjectServiceImpl implements OperationProjectService {

    @Resource
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Resource
    private OperationCrudSupport crudSupport;
    @Resource
    private OperationServerBindingSupport serverBindingSupport;
    @Resource
    private OperationServerCascadeSupport serverCascadeSupport;
    @Resource
    private OperationPortMatrixProvider portMatrixProvider;

    @Override
    public PageRes<OperationProjectVo> list(OperationProjectDeployInfo query) {
        LambdaQueryWrapper<OperationProjectDeployInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getProjectName())) {
            wrapper.like(OperationProjectDeployInfo::getProjectName, query.getProjectName());
        }
        if (StringUtils.isNotBlank(query.getServerIp())) {
            wrapper.like(OperationProjectDeployInfo::getServerIp, query.getServerIp());
        }
        if (query.getEnvironment() != null) {
            wrapper.eq(OperationProjectDeployInfo::getEnvironment, query.getEnvironment());
        }
        wrapper.orderByDesc(OperationProjectDeployInfo::getCreateTime);
        return crudSupport.selectPage(operationProjectDeployInfoMapper, wrapper,
                query.getPageNum(), query.getPageSize(), this::toVo);
    }

    @Override
    public OperationProjectVo getById(Long id) {
        return toVo(requireRow(id));
    }

    @Override
    public void create(OperationProjectSaveRequest request) {
        OperationProjectDeployInfo row = OperationSaveRequestMapper.toEntity(request);
        serverBindingSupport.bindProject(row);
        operationProjectDeployInfoMapper.insert(row);
    }

    @Override
    public void update(OperationProjectSaveRequest request) {
        crudSupport.assertUpdateId(request.getId());
        requireRow(request.getId());
        OperationProjectDeployInfo row = OperationSaveRequestMapper.toEntity(request);
        serverBindingSupport.bindProject(row);
        operationProjectDeployInfoMapper.updateById(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(Long[] ids) {
        crudSupport.deleteEach(ids,
                serverCascadeSupport::onDeleteProject,
                operationProjectDeployInfoMapper::deleteById);
    }

    @Override
    public void syncServerIdFromIp(OperationProjectDeployInfo row) {
        serverBindingSupport.bindProject(row);
    }

    private OperationProjectDeployInfo requireRow(Long id) {
        return crudSupport.requireRow(operationProjectDeployInfoMapper, id, "项目");
    }

    private OperationProjectVo toVo(OperationProjectDeployInfo row) {
        OperationProjectVo vo = new OperationProjectVo();
        BeanUtils.copyProperties(row, vo);
        OperationPortMatrixPortCheck portCheck = portMatrixProvider.check(row.getProjectName(), row.getPort());
        vo.setExpectedPort(portCheck.expectedPort);
        vo.setPortMatchStatus(portCheck.status);
        vo.setDeployRunning(row.getDeployRunning());
        vo.setLastDeployCheckTime(row.getLastDeployCheckTime());
        return vo;
    }
}
