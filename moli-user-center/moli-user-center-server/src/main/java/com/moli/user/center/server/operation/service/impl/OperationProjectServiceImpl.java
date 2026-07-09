package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.exception.BaseException;
import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.vo.OperationProjectVo;
import com.moli.user.center.server.operation.audit.OperationPortMatrix;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.service.OperationProjectService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationProjectServiceImpl implements OperationProjectService {

    @Resource
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Resource
    private OperationServerMapper operationServerMapper;

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

        Page<OperationProjectDeployInfo> page = new Page<>();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());
        operationProjectDeployInfoMapper.selectPage(page, wrapper);

        PageRes<OperationProjectVo> result = new PageRes<>();
        result.setTotal((int) page.getTotal());
        result.setPageNum(query.getPageNum());
        result.setPageSize(query.getPageSize());
        List<OperationProjectVo> list = page.getRecords().stream().map(this::toVo).collect(Collectors.toList());
        result.setList(list);
        return result;
    }

    @Override
    public OperationProjectVo getById(Long id) {
        return toVo(requireRow(id));
    }

    @Override
    public void create(OperationProjectDeployInfo form) {
        OperationProjectDeployInfo row = copyWritableFields(form);
        syncServerIdFromIp(row);
        operationProjectDeployInfoMapper.insert(row);
    }

    @Override
    public void update(OperationProjectDeployInfo form) {
        requireRow(form.getId());
        OperationProjectDeployInfo row = copyWritableFields(form);
        syncServerIdFromIp(row);
        operationProjectDeployInfoMapper.updateById(row);
    }

    @Override
    public void deleteByIds(Long[] ids) {
        for (Long id : ids) {
            operationProjectDeployInfoMapper.deleteById(id);
        }
    }

    @Override
    public void syncServerIdFromIp(OperationProjectDeployInfo row) {
        if (row.getServerId() != null || StringUtils.isBlank(row.getServerIp())) {
            return;
        }
        LambdaQueryWrapper<OperationServerInfo> byIp = new LambdaQueryWrapper<>();
        byIp.eq(OperationServerInfo::getIp, row.getServerIp());
        OperationServerInfo server = operationServerMapper.selectOne(byIp);
        if (server == null) {
            LambdaQueryWrapper<OperationServerInfo> byInner = new LambdaQueryWrapper<>();
            byInner.eq(OperationServerInfo::getInnerIp, row.getServerIp());
            server = operationServerMapper.selectOne(byInner);
        }
        if (server != null) {
            row.setServerId(server.getId());
        }
    }

    private OperationProjectDeployInfo requireRow(Long id) {
        OperationProjectDeployInfo row = operationProjectDeployInfoMapper.selectById(id);
        if (row == null) {
            throw new BaseException("项目不存在");
        }
        return row;
    }

    private OperationProjectDeployInfo copyWritableFields(OperationProjectDeployInfo form) {
        OperationProjectDeployInfo row = new OperationProjectDeployInfo();
        row.setId(form.getId());
        row.setServerId(form.getServerId());
        row.setServerIp(form.getServerIp());
        row.setInnerIp(form.getInnerIp());
        row.setUrl(form.getUrl());
        row.setProjectName(form.getProjectName());
        row.setDeployPath(form.getDeployPath());
        row.setPort(form.getPort());
        row.setEnvironment(form.getEnvironment());
        row.setRemark(form.getRemark());
        return row;
    }

    private OperationProjectVo toVo(OperationProjectDeployInfo row) {
        OperationProjectVo vo = new OperationProjectVo();
        BeanUtils.copyProperties(row, vo);
        OperationPortMatrix.PortCheck portCheck = OperationPortMatrix.check(row.getProjectName(), row.getPort());
        vo.setExpectedPort(portCheck.expectedPort);
        vo.setPortMatchStatus(portCheck.status);
        vo.setDeployRunning(row.getDeployRunning());
        vo.setLastDeployCheckTime(row.getLastDeployCheckTime());
        return vo;
    }
}
