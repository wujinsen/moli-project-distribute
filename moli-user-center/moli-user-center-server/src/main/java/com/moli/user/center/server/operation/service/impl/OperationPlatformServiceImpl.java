package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.exception.BaseException;
import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.entity.OperationPlatformInfo;
import com.moli.user.center.common.domain.vo.OperationPlatformVo;
import com.moli.user.center.common.domain.vo.OperationSecretRevealVo;
import com.moli.user.center.server.operation.mapper.OperationPlatformMapper;
import com.moli.user.center.server.operation.service.OperationPlatformService;
import com.moli.user.center.server.operation.support.OperationSecretSupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationPlatformServiceImpl implements OperationPlatformService {

    @Resource
    private OperationPlatformMapper operationPlatformMapper;
    @Resource
    private OperationSecretSupport secretSupport;

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

        Page<OperationPlatformInfo> page = new Page<>();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());
        operationPlatformMapper.selectPage(page, wrapper);

        PageRes<OperationPlatformVo> result = new PageRes<>();
        result.setTotal((int) page.getTotal());
        result.setPageNum(query.getPageNum());
        result.setPageSize(query.getPageSize());
        List<OperationPlatformVo> list = page.getRecords().stream().map(this::toVo).collect(Collectors.toList());
        result.setList(list);
        return result;
    }

    @Override
    public OperationPlatformVo getById(Long id) {
        OperationPlatformInfo row = requireRow(id);
        return toVo(row);
    }

    @Override
    public void create(OperationPlatformInfo form) {
        OperationPlatformInfo row = copyWritableFields(form);
        row.setPassword(secretSupport.encryptForStorage(form.getPassword()));
        operationPlatformMapper.insert(row);
    }

    @Override
    public void update(OperationPlatformInfo form) {
        OperationPlatformInfo existing = requireRow(form.getId());
        OperationPlatformInfo row = copyWritableFields(form);
        if (StringUtils.isNotBlank(form.getPassword())) {
            row.setPassword(secretSupport.encryptForStorage(form.getPassword()));
        } else {
            row.setPassword(existing.getPassword());
        }
        operationPlatformMapper.updateById(row);
    }

    @Override
    public void deleteByIds(Long[] ids) {
        for (Long id : ids) {
            operationPlatformMapper.deleteById(id);
        }
    }

    @Override
    public OperationSecretRevealVo revealPassword(Long id) {
        OperationPlatformInfo row = requireRow(id);
        return new OperationSecretRevealVo(secretSupport.resolvePlain(row.getPassword()));
    }

    private OperationPlatformInfo requireRow(Long id) {
        OperationPlatformInfo row = operationPlatformMapper.selectById(id);
        if (row == null) {
            throw new BaseException("运维平台不存在");
        }
        return row;
    }

    private OperationPlatformInfo copyWritableFields(OperationPlatformInfo form) {
        OperationPlatformInfo row = new OperationPlatformInfo();
        row.setId(form.getId());
        row.setPlatformName(form.getPlatformName());
        row.setUrl(form.getUrl());
        row.setAccount(form.getAccount());
        row.setEnvironment(form.getEnvironment());
        row.setRemark(form.getRemark());
        return row;
    }

    private OperationPlatformVo toVo(OperationPlatformInfo row) {
        OperationPlatformVo vo = new OperationPlatformVo();
        BeanUtils.copyProperties(row, vo);
        vo.setPasswordConfigured(secretSupport.hasSecret(row.getPassword()));
        vo.setPasswordMask(secretSupport.mask(row.getPassword()));
        return vo;
    }
}
