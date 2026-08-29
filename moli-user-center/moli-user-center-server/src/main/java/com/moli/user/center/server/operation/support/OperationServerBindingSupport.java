package com.moli.user.center.server.operation.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.server.operation.health.OperationHealthStatus;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目/组件与服务器台账的 server_id ↔ IP 对齐。
 */
@Component
@Slf4j
public class OperationServerBindingSupport {

    @Resource
    private OperationServerMapper operationServerMapper;

    public void bindProject(OperationProjectDeployInfo row) {
        if (row == null) {
            return;
        }
        if (row.getServerId() != null) {
            OperationServerInfo server = requireServer(row.getServerId());
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
        OperationServerInfo server = pickBestMatch(operationServerMapper.selectList(byIp), ip, "ip");
        if (server != null) {
            return server;
        }
        LambdaQueryWrapper<OperationServerInfo> byInner = new LambdaQueryWrapper<>();
        byInner.eq(OperationServerInfo::getInnerIp, ip);
        return pickBestMatch(operationServerMapper.selectList(byInner), ip, "inner_ip");
    }

    /**
     * 台账允许历史 smoke 数据重复 IP；探活回填 serverId 时不能因 selectOne 抛 TooManyResultsException。
     * 多行时优先 UP，再取最小 id（稳定、可预期）。
     */
    OperationServerInfo pickBestMatch(List<OperationServerInfo> matches, String ip, String field) {
        if (matches == null || matches.isEmpty()) {
            return null;
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }
        String ids = matches.stream()
                .map(OperationServerInfo::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        log.warn("operation_server_info duplicate {}={}: matched {} rows (ids={}), picking best",
                field, ip, matches.size(), ids);
        return matches.stream()
                .min(Comparator
                        .comparing((OperationServerInfo s) ->
                                s.getStatus() != null && s.getStatus() == OperationHealthStatus.UP ? 0 : 1)
                        .thenComparing(OperationServerInfo::getId, Comparator.nullsLast(Long::compareTo)))
                .orElse(matches.get(0));
    }

    private OperationServerInfo requireServer(Long serverId) {
        OperationServerInfo server = operationServerMapper.selectById(serverId);
        if (server == null) {
            throw OperationBizException.serverNotFound(serverId);
        }
        return server;
    }
}
