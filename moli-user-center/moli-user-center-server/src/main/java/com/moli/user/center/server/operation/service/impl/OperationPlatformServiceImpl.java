package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.dto.operation.OperationPlatformSaveRequest;
import com.moli.user.center.common.domain.entity.OperationPlatformInfo;
import com.moli.user.center.common.domain.vo.OperationPlatformVo;
import com.moli.user.center.common.domain.vo.OperationSecretRevealVo;
import com.moli.user.center.server.operation.mapper.OperationPlatformMapper;
import com.moli.user.center.server.operation.service.OperationPlatformService;
import com.moli.user.center.server.operation.support.OperationCrudSupport;
import com.moli.user.center.server.operation.support.OperationSaveRequestMapper;
import com.moli.user.center.server.operation.support.OperationSecretCrudSupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OperationPlatformServiceImpl implements OperationPlatformService {

    @Resource
    private OperationPlatformMapper operationPlatformMapper;
    @Resource
    private OperationCrudSupport crudSupport;
    @Resource
    private OperationSecretCrudSupport secretCrudSupport;

    @Override
    public PageRes<OperationPlatformVo> list(OperationPlatformInfo query) {
        LambdaQueryWrapper<OperationPlatformInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getPlatformName())) {
            wrapper.like(OperationPlatformInfo::getPlatformName, query.getPlatformName());
        }
        if (query.getEnvironment() != null) {
            wrapper.eq(OperationPlatformInfo::getEnvironment, query.getEnvironment());
        }
        wrapper.orderByDesc(OperationPlatformInfo::getCreateTime);
        return crudSupport.selectPage(operationPlatformMapper, wrapper,
                query.getPageNum(), query.getPageSize(), this::toVo);
    }

    @Override
    public OperationPlatformVo getById(Long id) {
        return toVo(requireRow(id));
    }

    @Override
    public void create(OperationPlatformSaveRequest request) {
        OperationPlatformInfo row = OperationSaveRequestMapper.toEntity(request);
        row.setPassword(secretCrudSupport.encryptOnSave(OperationSaveRequestMapper.password(request)));
        operationPlatformMapper.insert(row);
    }

    @Override
    public void update(OperationPlatformSaveRequest request) {
        crudSupport.assertUpdateId(request.getId());
        OperationPlatformInfo existing = requireRow(request.getId());
        OperationPlatformInfo row = OperationSaveRequestMapper.toEntity(request);
        row.setPassword(secretCrudSupport.mergeOnUpdate(request.getPassword(), existing.getPassword()));
        operationPlatformMapper.updateById(row);
    }

    @Override
    public void deleteByIds(Long[] ids) {
        crudSupport.deleteEach(ids, operationPlatformMapper::deleteById);
    }

    @Override
    public OperationSecretRevealVo revealPassword(Long id) {
        return secretCrudSupport.reveal(requireRow(id).getPassword());
    }

    private OperationPlatformInfo requireRow(Long id) {
        return crudSupport.requireRow(operationPlatformMapper, id, "运维平台");
    }

    private OperationPlatformVo toVo(OperationPlatformInfo row) {
        OperationPlatformVo vo = new OperationPlatformVo();
        BeanUtils.copyProperties(row, vo);
        vo.setPasswordConfigured(secretCrudSupport.passwordConfigured(row.getPassword()));
        vo.setPasswordMask(secretCrudSupport.passwordMask(row.getPassword()));
        return vo;
    }
}
