package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.exception.BaseException;
import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.vo.OperationComponentTopologyItemVo;
import com.moli.user.center.common.domain.vo.OperationProjectDeployInfoVo;
import com.moli.user.center.common.domain.vo.OperationServerInfoVo;
import com.moli.user.center.common.domain.vo.OperationServerSshVo;
import com.moli.user.center.common.domain.vo.OperationServerTopologyVo;
import com.moli.user.center.common.domain.vo.OperationServerVo;
import com.moli.user.center.common.domain.vo.OperationSshTestVo;
import com.moli.user.center.server.operation.health.OperationHealthStatus;
import com.moli.user.center.server.operation.health.OperationTcpProbe;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.service.OperationServerService;
import com.moli.user.center.server.operation.ssh.OperationSshAuthType;
import com.moli.user.center.server.operation.ssh.OperationSshClient;
import com.moli.user.center.server.operation.ssh.OperationSshCommandResult;
import com.moli.user.center.server.operation.ssh.OperationSshSession;
import com.moli.user.center.server.operation.support.OperationSecretSupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OperationServerServiceImpl implements OperationServerService {

    @Resource
    private OperationServerMapper operationServerMapper;
    @Resource
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Resource
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;
    @Resource
    private OperationServerLinkMapper operationServerLinkMapper;
    @Resource
    private OperationSecretSupport secretSupport;
    @Resource
    private OperationSshClient sshClient;

    @Override
    public PageRes<OperationServerVo> list(OperationServerInfoVo query) {
        LambdaQueryWrapper<OperationServerInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getServerName())) {
            wrapper.like(OperationServerInfo::getServerName, query.getServerName());
        }
        if (StringUtils.isNotBlank(query.getIp())) {
            wrapper.like(OperationServerInfo::getIp, query.getIp());
        }
        if (query.getEnvironment() != null) {
            wrapper.eq(OperationServerInfo::getEnvironment, query.getEnvironment());
        }
        wrapper.orderByDesc(OperationServerInfo::getCreateTime);

        Page<OperationServerInfo> page = new Page<>();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());
        operationServerMapper.selectPage(page, wrapper);

        PageRes<OperationServerVo> result = new PageRes<>();
        result.setTotal((int) page.getTotal());
        result.setPageNum(query.getPageNum());
        result.setPageSize(query.getPageSize());
        result.setList(page.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return result;
    }

    @Override
    public OperationServerVo getById(Long id) {
        return toVo(requireRow(id));
    }

    @Override
    public void create(OperationServerInfo form) {
        OperationServerInfo row = copyWritableFields(form);
        if (row.getStatus() == null) {
            row.setStatus(OperationHealthStatus.UNKNOWN);
        }
        operationServerMapper.insert(row);
    }

    @Override
    public void update(OperationServerInfo form) {
        OperationServerInfo existing = requireRow(form.getId());
        OperationServerInfo row = copyWritableFields(form);
        row.setStatus(existing.getStatus());
        row.setLastCheckTime(existing.getLastCheckTime());
        operationServerMapper.updateById(row);
    }

    @Override
    public void deleteByIds(Long[] ids) {
        for (Long id : ids) {
            operationServerMapper.deleteById(id);
        }
    }

    @Override
    public OperationServerTopologyVo getTopology(Long id) {
        OperationServerInfo server = requireRow(id);
        OperationServerTopologyVo topology = new OperationServerTopologyVo();
        topology.setServer(toVo(server));
        topology.setProjects(loadProjects(server));
        topology.setComponents(loadComponents(server));
        return topology;
    }

    @Override
    public OperationServerVo checkHealth(Long id) {
        OperationServerInfo row = requireRow(id);
        int status = OperationTcpProbe.probe(row.getIp(), row.getPort());
        row.setStatus(status);
        row.setLastCheckTime(new Date());
        operationServerMapper.updateById(row);
        return toVo(row);
    }

    @Override
    public void saveSsh(Long id, OperationServerSshVo form) {
        OperationServerInfo existing = requireRow(id);
        OperationServerInfo row = new OperationServerInfo();
        row.setId(id);
        row.setSshPort(form.getSshPort() != null ? form.getSshPort() : 22);
        row.setSshUser(StringUtils.isNotBlank(form.getSshUser()) ? form.getSshUser().trim() : "ubuntu");
        row.setSshAuthType(form.getSshAuthType() != null ? form.getSshAuthType() : OperationSshAuthType.PRIVATE_KEY);
        row.setConnPref(StringUtils.isNotBlank(form.getConnPref()) ? form.getConnPref().trim() : "auto");
        if (form.getUploadAllowedRoots() != null) {
            row.setUploadAllowedRoots(StringUtils.trimToNull(form.getUploadAllowedRoots()));
        } else {
            row.setUploadAllowedRoots(existing.getUploadAllowedRoots());
        }
        // 私钥/密码留空表示不修改，沿用旧密文
        if (StringUtils.isNotBlank(form.getPrivateKey())) {
            row.setSshPrivateKey(secretSupport.encryptForStorage(form.getPrivateKey().trim()));
        } else {
            row.setSshPrivateKey(existing.getSshPrivateKey());
        }
        if (StringUtils.isNotBlank(form.getPassphrase())) {
            row.setSshPassphrase(secretSupport.encryptForStorage(form.getPassphrase().trim()));
        } else {
            row.setSshPassphrase(existing.getSshPassphrase());
        }
        operationServerMapper.updateById(row);
    }

    @Override
    public OperationSshTestVo testSsh(Long id) {
        OperationServerInfo row = requireRow(id);
        OperationSshTestVo vo = new OperationSshTestVo();
        long start = System.currentTimeMillis();
        try (OperationSshSession session = sshClient.connect(row)) {
            OperationSshCommandResult result = sshClient.exec(session, "whoami && hostname", null);
            vo.setSuccess(result.isSuccess());
            vo.setHost(session.getHost());
            vo.setOutput(result.getOutput());
            vo.setMessage(result.isSuccess() ? "连接成功" : "命令返回非零退出码");
        } catch (Exception e) {
            vo.setSuccess(false);
            vo.setMessage(e.getMessage());
        } finally {
            vo.setElapsedMs(System.currentTimeMillis() - start);
        }
        return vo;
    }

    @Override
    public OperationServerInfo requireEntity(Long id) {
        return requireRow(id);
    }

    private List<OperationProjectDeployInfoVo> loadProjects(OperationServerInfo server) {
        Set<Long> projectIds = new LinkedHashSet<>(operationServerLinkMapper.selectProjectIdsByServerId(server.getId()));
        LambdaQueryWrapper<OperationProjectDeployInfo> byServerId = new LambdaQueryWrapper<>();
        byServerId.eq(OperationProjectDeployInfo::getServerId, server.getId());
        operationProjectDeployInfoMapper.selectList(byServerId).forEach(p -> projectIds.add(p.getId()));

        if (projectIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<OperationProjectDeployInfo> rows = operationProjectDeployInfoMapper.selectBatchIds(projectIds);
        Map<Long, OperationProjectDeployInfoVo> ordered = new LinkedHashMap<>();
        for (OperationProjectDeployInfo row : rows) {
            OperationProjectDeployInfoVo vo = new OperationProjectDeployInfoVo();
            BeanUtils.copyProperties(row, vo);
            ordered.put(row.getId(), vo);
        }
        return new ArrayList<>(ordered.values());
    }

    private List<OperationComponentTopologyItemVo> loadComponents(OperationServerInfo server) {
        Set<Long> componentIds = new LinkedHashSet<>(operationServerLinkMapper.selectComponentIdsByServerId(server.getId()));

        LambdaQueryWrapper<OperationComponentDeployInfo> byIp = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(server.getIp())) {
            byIp.eq(OperationComponentDeployInfo::getServerIp, server.getIp());
        } else if (StringUtils.isNotBlank(server.getInnerIp())) {
            byIp.eq(OperationComponentDeployInfo::getServerIp, server.getInnerIp());
        }
        if (StringUtils.isNotBlank(server.getIp()) || StringUtils.isNotBlank(server.getInnerIp())) {
            operationComponentDeployInfoMapper.selectList(byIp).forEach(c -> componentIds.add(c.getId()));
        }

        if (componentIds.isEmpty()) {
            return new ArrayList<>();
        }
        return operationComponentDeployInfoMapper.selectBatchIds(componentIds).stream()
                .map(this::toComponentTopologyItem)
                .collect(Collectors.toList());
    }

    private OperationComponentTopologyItemVo toComponentTopologyItem(OperationComponentDeployInfo row) {
        OperationComponentTopologyItemVo vo = new OperationComponentTopologyItemVo();
        vo.setId(row.getId());
        vo.setComponentName(row.getComponentName());
        vo.setServerIp(row.getServerIp());
        vo.setPort(row.getPort());
        vo.setVersion(row.getVersion());
        vo.setDeployPath(row.getDeployPath());
        vo.setEnvironment(row.getEnvironment());
        vo.setStatus(row.getStatus());
        vo.setLastCheckTime(row.getLastCheckTime());
        return vo;
    }

    private OperationServerInfo requireRow(Long id) {
        OperationServerInfo row = operationServerMapper.selectById(id);
        if (row == null) {
            throw new BaseException("服务器不存在");
        }
        return row;
    }

    private OperationServerInfo copyWritableFields(OperationServerInfo form) {
        OperationServerInfo row = new OperationServerInfo();
        row.setId(form.getId());
        row.setServerName(form.getServerName());
        row.setIp(form.getIp());
        row.setInnerIp(form.getInnerIp());
        row.setPort(form.getPort());
        row.setEnvironment(form.getEnvironment());
        row.setRemark(form.getRemark());
        return row;
    }

    private OperationServerVo toVo(OperationServerInfo row) {
        OperationServerVo vo = new OperationServerVo();
        BeanUtils.copyProperties(row, vo);
        // 私钥/密码不回显，仅暴露是否已配置
        vo.setSshConfigured(row.getSshAuthType() != null
                && (StringUtils.isNotBlank(row.getSshPrivateKey()) || StringUtils.isNotBlank(row.getSshPassphrase())));
        return vo;
    }
}
