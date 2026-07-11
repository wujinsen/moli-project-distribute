package com.moli.user.center.server.operation.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 项目/组件与服务器台账的 server_id ↔ IP 对齐。
 */
@Component
public class OperationServerBindingSupport {

    @Resource
    private OperationServerMapper operationServerMapper;

    public void bindProject(OperationProjectDeployInfo row) {
        if (row == null) {
            return;
        }
        if (row.getServerId() != null) {
            OperationServerInfo server = requireServer(row.getServerId());
            if (StringUtils.isNotBlank(row.getServerIp()) && !matchesIp(row.getServerIp(), server)) {
                throw OperationBizException.params("serverIp 与 serverId 不一致，请以 serverId 为准");
            }
            row.setServerIp(server.getIp());
            row.setInnerIp(server.getInnerIp());
            return;
        }
        if (StringUtils.isBlank(row.getServerIp())) {
            return;
        }
        OperationServerInfo server = findByIp(row.getServerIp());
        if (server != null) {
            row.setServerId(server.getId());
            row.setServerIp(server.getIp());
            row.setInnerIp(server.getInnerIp());
        }
    }

    public void bindComponent(OperationComponentDeployInfo row) {
        if (row == null) {
            return;
        }
        if (row.getServerId() != null) {
            OperationServerInfo server = requireServer(row.getServerId());
            if (StringUtils.isNotBlank(row.getServerIp()) && !matchesIp(row.getServerIp(), server)) {
                throw OperationBizException.params("serverIp 与 serverId 不一致，请以 serverId 为准");
            }
            row.setServerIp(server.getIp());
            return;
        }
        if (StringUtils.isBlank(row.getServerIp())) {
            return;
        }
        OperationServerInfo server = findByIp(row.getServerIp());
        if (server != null) {
            row.setServerId(server.getId());
            row.setServerIp(server.getIp());
        }
    }

    private OperationServerInfo findByIp(String ip) {
        LambdaQueryWrapper<OperationServerInfo> byIp = new LambdaQueryWrapper<>();
        byIp.eq(OperationServerInfo::getIp, ip);
        OperationServerInfo server = operationServerMapper.selectOne(byIp);
        if (server != null) {
            return server;
        }
        LambdaQueryWrapper<OperationServerInfo> byInner = new LambdaQueryWrapper<>();
        byInner.eq(OperationServerInfo::getInnerIp, ip);
        return operationServerMapper.selectOne(byInner);
    }

    private OperationServerInfo requireServer(Long serverId) {
        OperationServerInfo server = operationServerMapper.selectById(serverId);
        if (server == null) {
            throw OperationBizException.serverNotFound(serverId);
        }
        return server;
    }

    private boolean matchesIp(String ip, OperationServerInfo server) {
        return StringUtils.equals(ip, server.getIp()) || StringUtils.equals(ip, server.getInnerIp());
    }
}
