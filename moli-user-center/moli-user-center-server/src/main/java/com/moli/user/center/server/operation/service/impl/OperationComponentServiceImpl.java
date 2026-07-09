package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.exception.BaseException;
import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.vo.OperationComponentVo;
import com.moli.user.center.common.domain.vo.OperationSecretRevealVo;
import com.moli.user.center.server.operation.audit.OperationPortMatrix;
import com.moli.user.center.server.operation.health.OperationHealthStatus;
import com.moli.user.center.server.operation.health.OperationTcpProbe;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.service.OperationComponentService;
import com.moli.user.center.server.operation.support.OperationSecretSupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationComponentServiceImpl implements OperationComponentService {

    @Resource
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;
    @Resource
    private OperationSecretSupport secretSupport;

    @Override
    public PageRes<OperationComponentVo> list(OperationComponentDeployInfo query) {
        LambdaQueryWrapper<OperationComponentDeployInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getComponentName())) {
            wrapper.like(OperationComponentDeployInfo::getComponentName, query.getComponentName());
        }
        if (StringUtils.isNotBlank(query.getServerIp())) {
            wrapper.like(OperationComponentDeployInfo::getServerIp, query.getServerIp());
        }
        if (query.getEnvironment() != null) {
            wrapper.eq(OperationComponentDeployInfo::getEnvironment, query.getEnvironment());
        }
        wrapper.orderByDesc(OperationComponentDeployInfo::getCreateTime);

        Page<OperationComponentDeployInfo> page = new Page<>();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());
        operationComponentDeployInfoMapper.selectPage(page, wrapper);

        PageRes<OperationComponentVo> result = new PageRes<>();
        result.setTotal((int) page.getTotal());
        result.setPageNum(query.getPageNum());
        result.setPageSize(query.getPageSize());
        List<OperationComponentVo> list = page.getRecords().stream().map(this::toVo).collect(Collectors.toList());
        result.setList(list);
        return result;
    }

    @Override
    public OperationComponentVo getById(Long id) {
        OperationComponentDeployInfo row = requireRow(id);
        return toVo(row);
    }

    @Override
    public void create(OperationComponentDeployInfo form) {
        OperationComponentDeployInfo row = copyWritableFields(form);
        row.setPassword(secretSupport.encryptForStorage(form.getPassword()));
        if (row.getStatus() == null) {
            row.setStatus(OperationHealthStatus.UNKNOWN);
        }
        operationComponentDeployInfoMapper.insert(row);
    }

    @Override
    public void update(OperationComponentDeployInfo form) {
        OperationComponentDeployInfo existing = requireRow(form.getId());
        OperationComponentDeployInfo row = copyWritableFields(form);
        if (StringUtils.isNotBlank(form.getPassword())) {
            row.setPassword(secretSupport.encryptForStorage(form.getPassword()));
        } else {
            row.setPassword(existing.getPassword());
        }
        row.setStatus(existing.getStatus());
        row.setLastCheckTime(existing.getLastCheckTime());
        operationComponentDeployInfoMapper.updateById(row);
    }

    @Override
    public void deleteByIds(Long[] ids) {
        for (Long id : ids) {
            operationComponentDeployInfoMapper.deleteById(id);
        }
    }

    @Override
    public OperationSecretRevealVo revealPassword(Long id) {
        OperationComponentDeployInfo row = requireRow(id);
        return new OperationSecretRevealVo(secretSupport.resolvePlain(row.getPassword()));
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
        OperationComponentDeployInfo row = operationComponentDeployInfoMapper.selectById(id);
        if (row == null) {
            throw new BaseException("运维组件不存在");
        }
        return row;
    }

    private OperationComponentDeployInfo copyWritableFields(OperationComponentDeployInfo form) {
        OperationComponentDeployInfo row = new OperationComponentDeployInfo();
        row.setId(form.getId());
        row.setComponentName(form.getComponentName());
        row.setServerIp(form.getServerIp());
        row.setAccount(form.getAccount());
        row.setDeployPath(form.getDeployPath());
        row.setPort(form.getPort());
        row.setVersion(form.getVersion());
        row.setEnvironment(form.getEnvironment());
        row.setRemark(form.getRemark());
        return row;
    }

    private OperationComponentVo toVo(OperationComponentDeployInfo row) {
        OperationComponentVo vo = new OperationComponentVo();
        BeanUtils.copyProperties(row, vo);
        vo.setPasswordConfigured(secretSupport.hasSecret(row.getPassword()));
        vo.setPasswordMask(secretSupport.mask(row.getPassword()));
        vo.setStatus(row.getStatus());
        vo.setLastCheckTime(row.getLastCheckTime());
        OperationPortMatrix.PortCheck portCheck = OperationPortMatrix.check(row.getComponentName(), row.getPort());
        vo.setExpectedPort(portCheck.expectedPort);
        vo.setPortMatchStatus(portCheck.status);
        return vo;
    }
}
