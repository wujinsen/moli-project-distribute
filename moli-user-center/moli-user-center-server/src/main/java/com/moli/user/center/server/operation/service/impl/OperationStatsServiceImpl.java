package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.user.center.common.domain.entity.OperationComponentDeployInfo;
import com.moli.user.center.common.domain.entity.OperationPlatformInfo;
import com.moli.user.center.common.domain.entity.OperationProjectDeployInfo;
import com.moli.user.center.common.domain.entity.OperationServerInfo;
import com.moli.user.center.common.domain.vo.OperationEnvCountVo;
import com.moli.user.center.common.domain.vo.OperationStatsVo;
import com.moli.user.center.server.operation.health.OperationHealthStatus;
import com.moli.user.center.server.operation.mapper.OperationComponentDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationPlatformMapper;
import com.moli.user.center.server.operation.mapper.OperationProjectDeployInfoMapper;
import com.moli.user.center.server.operation.mapper.OperationServerMapper;
import com.moli.user.center.server.operation.service.OperationAuditService;
import com.moli.user.center.server.operation.service.OperationStatsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OperationStatsServiceImpl implements OperationStatsService {

    @Resource
    private OperationProjectDeployInfoMapper operationProjectDeployInfoMapper;
    @Resource
    private OperationServerMapper operationServerMapper;
    @Resource
    private OperationPlatformMapper operationPlatformMapper;
    @Resource
    private OperationComponentDeployInfoMapper operationComponentDeployInfoMapper;
    @Resource
    private OperationAuditService operationAuditService;

    @Override
    public OperationStatsVo getStats() {
        OperationStatsVo stats = new OperationStatsVo();
        stats.setProjects(count(operationProjectDeployInfoMapper));
        stats.setServers(count(operationServerMapper));
        stats.setPlatforms(count(operationPlatformMapper));
        stats.setComponents(count(operationComponentDeployInfoMapper));
        stats.setPortMismatches(operationAuditService.auditPortMatrix().getMismatched());
        stats.setHealthDown(countHealthDown());
        stats.setEnvBreakdown(buildEnvBreakdown());
        return stats;
    }

    private int countHealthDown() {
        LambdaQueryWrapper<OperationServerInfo> serverDown = new LambdaQueryWrapper<>();
        serverDown.eq(OperationServerInfo::getStatus, OperationHealthStatus.DOWN);
        int servers = operationServerMapper.selectCount(serverDown).intValue();

        LambdaQueryWrapper<OperationComponentDeployInfo> componentDown = new LambdaQueryWrapper<>();
        componentDown.eq(OperationComponentDeployInfo::getStatus, OperationHealthStatus.DOWN);
        int components = operationComponentDeployInfoMapper.selectCount(componentDown).intValue();
        return servers + components;
    }

    private List<OperationEnvCountVo> buildEnvBreakdown() {
        Map<Integer, Integer> counts = new LinkedHashMap<>();
        for (int env = 1; env <= 4; env++) {
            counts.put(env, 0);
        }
        accumulateEnv(counts, operationProjectDeployInfoMapper.selectList(null));
        accumulateEnv(counts, operationServerMapper.selectList(null));
        accumulateEnv(counts, operationPlatformMapper.selectList(null));
        accumulateEnv(counts, operationComponentDeployInfoMapper.selectList(null));

        List<OperationEnvCountVo> rows = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            OperationEnvCountVo row = new OperationEnvCountVo();
            row.setEnv(entry.getKey());
            row.setCount(entry.getValue());
            rows.add(row);
        }
        return rows;
    }

    private void accumulateEnv(Map<Integer, Integer> counts, List<?> rows) {
        for (Object row : rows) {
            Integer env = readEnvironment(row);
            if (env == null) {
                continue;
            }
            counts.merge(env, 1, Integer::sum);
        }
    }

    private Integer readEnvironment(Object row) {
        if (row instanceof OperationProjectDeployInfo) {
            return ((OperationProjectDeployInfo) row).getEnvironment();
        }
        if (row instanceof OperationServerInfo) {
            return ((OperationServerInfo) row).getEnvironment();
        }
        if (row instanceof OperationPlatformInfo) {
            return ((OperationPlatformInfo) row).getEnvironment();
        }
        if (row instanceof OperationComponentDeployInfo) {
            return ((OperationComponentDeployInfo) row).getEnvironment();
        }
        return null;
    }

    private int count(com.baomidou.mybatisplus.core.mapper.BaseMapper<?> mapper) {
        return mapper.selectCount(null).intValue();
    }
}
