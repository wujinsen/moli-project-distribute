package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.dto.operation.OperationServerSaveRequest;
import com.moli.user.center.common.domain.dto.operation.OperationServerRoles;
import com.moli.user.center.common.domain.dto.operation.OperationServerTagsSupport;
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
import com.moli.user.center.common.domain.entity.OperationTask;
import com.moli.user.center.server.operation.health.OperationHealthStatus;
import com.moli.user.center.server.operation.health.OperationTcpProbe;
import com.moli.user.center.server.operation.mapper.OperationTaskMapper;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerLinkMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.service.OperationServerService;
import com.moli.user.center.server.operation.ssh.OperationSshAuthType;
import com.moli.user.center.server.operation.ssh.OperationSshClient;
import com.moli.user.center.server.operation.ssh.OperationSshCommandResult;
import com.moli.user.center.server.operation.ssh.OperationSshSession;
import com.moli.user.center.server.operation.support.OperationBizException;
import com.moli.user.center.server.operation.support.OperationCrudSupport;
import com.moli.user.center.server.operation.support.OperationSaveRequestMapper;
import com.moli.user.center.server.operation.support.OperationSecretSupport;
import com.moli.user.center.server.operation.support.OperationServerCascadeSupport;
import com.moli.user.center.server.operation.task.OperationTaskStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private OperationCrudSupport crudSupport;
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
    @Resource
    private OperationServerCascadeSupport serverCascadeSupport;
    @Resource
    private OperationTaskMapper operationTaskMapper;

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
        if (StringUtils.isNotBlank(query.getServerRole())) {
            wrapper.eq(OperationServerInfo::getServerRole, query.getServerRole().trim());
        }
        if (StringUtils.isNotBlank(query.getTag())) {
            String tag = OperationServerTagsSupport.normalizeTag(query.getTag());
            if (tag != null && OperationServerTagsSupport.isValidTag(tag)) {
                wrapper.apply("JSON_CONTAINS(COALESCE(tags, '[]'), {0})", "\"" + tag + "\"");
            }
        }
        wrapper.orderByDesc(OperationServerInfo::getCreateTime);
        return crudSupport.selectPage(operationServerMapper, wrapper,
                query.getPageNum(), query.getPageSize(), this::toVo);
    }

    @Override
    public List<String> listTagOptions() {
        LambdaQueryWrapper<OperationServerInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(OperationServerInfo::getTags)
                .isNotNull(OperationServerInfo::getTags)
                .ne(OperationServerInfo::getTags, "");
        List<OperationServerInfo> rows = operationServerMapper.selectList(wrapper);
        List<List<String>> groups = new ArrayList<>();
        for (OperationServerInfo row : rows) {
            groups.add(OperationServerTagsSupport.parse(row.getTags()));
        }
        return OperationServerTagsSupport.mergeDistinct(groups);
    }

    @Override
    public OperationServerVo getById(Long id) {
        return toVo(requireRow(id));
    }

    @Override
    public void create(OperationServerSaveRequest request) {
        OperationServerInfo row = OperationSaveRequestMapper.toEntity(request);
        assertUniqueIp(row, null);
        if (row.getStatus() == null) {
            row.setStatus(OperationHealthStatus.UNKNOWN);
        }
        if (StringUtils.isBlank(row.getServerRole())) {
            row.setServerRole(OperationServerRoles.APP);
        }
        operationServerMapper.insert(row);
    }

    @Override
    public void update(OperationServerSaveRequest request) {
        crudSupport.assertUpdateId(request.getId());
        OperationServerInfo existing = requireRow(request.getId());
        OperationServerInfo row = OperationSaveRequestMapper.toEntity(request);
        assertUniqueIp(row, request.getId());
        row.setStatus(existing.getStatus());
        row.setLastCheckTime(existing.getLastCheckTime());
        operationServerMapper.updateById(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(Long[] ids) {
        crudSupport.deleteEach(ids, id -> {
            assertNoRunningTasks(id);
            serverCascadeSupport.onDeleteServer(id);
        }, operationServerMapper::deleteById);
    }

    private void assertNoRunningTasks(Long serverId) {
        LambdaQueryWrapper<OperationTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationTask::getServerId, serverId);
        wrapper.in(OperationTask::getStatus, OperationTaskStatus.PENDING, OperationTaskStatus.RUNNING);
        if (operationTaskMapper.selectCount(wrapper) > 0) {
            throw OperationBizException.serverTaskRunning(serverId);
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

        LambdaQueryWrapper<OperationComponentDeployInfo> byServerId = new LambdaQueryWrapper<>();
        byServerId.eq(OperationComponentDeployInfo::getServerId, server.getId());
        operationComponentDeployInfoMapper.selectList(byServerId).forEach(c -> componentIds.add(c.getId()));

        LambdaQueryWrapper<OperationComponentDeployInfo> byIp = new LambdaQueryWrapper<>();
        List<String> serverIps = collectServerIps(server);
        if (!serverIps.isEmpty()) {
            byIp.in(OperationComponentDeployInfo::getServerIp, serverIps);
            operationComponentDeployInfoMapper.selectList(byIp).forEach(c -> componentIds.add(c.getId()));
        }

        if (componentIds.isEmpty()) {
            return new ArrayList<>();
        }
        return operationComponentDeployInfoMapper.selectBatchIds(componentIds).stream()
                .map(this::toComponentTopologyItem)
                .collect(Collectors.toList());
    }

    private static List<String> collectServerIps(OperationServerInfo server) {
        List<String> ips = new ArrayList<>(2);
        if (StringUtils.isNotBlank(server.getIp())) {
            ips.add(server.getIp().trim());
        }
        if (StringUtils.isNotBlank(server.getInnerIp())) {
            String inner = server.getInnerIp().trim();
            if (!ips.contains(inner)) {
                ips.add(inner);
            }
        }
        return ips;
    }

    private OperationComponentTopologyItemVo toComponentTopologyItem(OperationComponentDeployInfo row) {
        OperationComponentTopologyItemVo vo = new OperationComponentTopologyItemVo();
        vo.setId(row.getId());
        vo.setComponentName(row.getComponentName());
        vo.setServerId(row.getServerId());
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
        return crudSupport.requireRow(operationServerMapper, id, "服务器");
    }

    private void assertUniqueIp(OperationServerInfo row, Long excludeId) {
        if (row.getEnvironment() == null || StringUtils.isBlank(row.getIp())) {
            return;
        }
        LambdaQueryWrapper<OperationServerInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationServerInfo::getEnvironment, row.getEnvironment());
        wrapper.eq(OperationServerInfo::getIp, row.getIp());
        if (excludeId != null) {
            wrapper.ne(OperationServerInfo::getId, excludeId);
        }
        if (operationServerMapper.selectCount(wrapper) > 0) {
            throw OperationBizException.duplicateIp(row.getIp(), row.getEnvironment());
        }
    }

    private OperationServerVo toVo(OperationServerInfo row) {
        OperationServerVo vo = new OperationServerVo();
        BeanUtils.copyProperties(row, vo);
        vo.setTags(OperationServerTagsSupport.parse(row.getTags()));
        // 私钥/密码不回显，仅暴露是否已配置
        vo.setSshConfigured(row.getSshAuthType() != null
                && (StringUtils.isNotBlank(row.getSshPrivateKey()) || StringUtils.isNotBlank(row.getSshPassphrase())));
        return vo;
    }
}
