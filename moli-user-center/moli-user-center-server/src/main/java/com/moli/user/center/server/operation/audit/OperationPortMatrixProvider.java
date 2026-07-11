package com.moli.user.center.server.operation.audit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moli.user.center.common.domain.entity.OperationPortMatrixAliasInfo;
import com.moli.user.center.common.domain.entity.OperationPortMatrixInfo;
import com.moli.user.center.common.domain.vo.OperationPortMatrixEntryVo;
import com.moli.user.center.server.operation.mapper.OperationPortMatrixAliasMapper;
import com.moli.user.center.server.operation.mapper.OperationPortMatrixMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 端口矩阵运行时缓存（SVR-21）：DB 启用行优先，空表回退内置默认。
 */
@Component
@Slf4j
public class OperationPortMatrixProvider {

    private final AtomicBoolean emptyTableWarned = new AtomicBoolean(false);
    private volatile OperationPortMatrixSnapshot snapshot = OperationPortMatrixDefaults.snapshot();

    @Resource
    private OperationPortMatrixMapper operationPortMatrixMapper;
    @Resource
    private OperationPortMatrixAliasMapper operationPortMatrixAliasMapper;

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        List<OperationPortMatrixInfo> matrices = operationPortMatrixMapper.selectList(
                new QueryWrapper<OperationPortMatrixInfo>()
                        .eq("enabled", 1)
                        .orderByAsc("sort_order", "matrix_key"));
        if (matrices == null || matrices.isEmpty()) {
            if (emptyTableWarned.compareAndSet(false, true)) {
                log.warn("operation_port_matrix 无启用记录，端口审计回退内置默认矩阵");
            }
            snapshot = OperationPortMatrixDefaults.snapshot();
            return;
        }
        emptyTableWarned.set(false);
        Map<Long, List<String>> aliasMap = loadAliases(matrices);
        List<OperationPortMatrixSnapshot.Row> rows = new ArrayList<>(matrices.size());
        for (OperationPortMatrixInfo matrix : matrices) {
            rows.add(new OperationPortMatrixSnapshot.Row(
                    matrix.getMatrixKey(),
                    matrix.getExpectedPort(),
                    defaultSource(matrix.getSource()),
                    matrix.getSortOrder() != null ? matrix.getSortOrder() : 0,
                    aliasMap.getOrDefault(matrix.getId(), Collections.emptyList())));
        }
        rows.sort(Comparator.comparingInt((OperationPortMatrixSnapshot.Row r) -> r.sortOrder)
                .thenComparing(r -> r.matrixKey));
        snapshot = OperationPortMatrixSnapshot.of(rows, false);
    }

    public OperationPortMatrixPortCheck check(String name, String port) {
        return snapshot.check(name, port);
    }

    public List<OperationPortMatrixEntryVo> auditEntries() {
        return snapshot.auditEntries();
    }

    public boolean isUsingDefaults() {
        return snapshot.isUsingDefaults();
    }

    private Map<Long, List<String>> loadAliases(List<OperationPortMatrixInfo> matrices) {
        List<Long> ids = matrices.stream().map(OperationPortMatrixInfo::getId).collect(Collectors.toList());
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<OperationPortMatrixAliasInfo> aliases = operationPortMatrixAliasMapper.selectList(
                new QueryWrapper<OperationPortMatrixAliasInfo>()
                        .in("matrix_id", ids)
                        .orderByAsc("alias"));
        Map<Long, List<String>> map = new HashMap<>();
        for (OperationPortMatrixAliasInfo alias : aliases) {
            map.computeIfAbsent(alias.getMatrixId(), k -> new ArrayList<>()).add(alias.getAlias());
        }
        return map;
    }

    private static String defaultSource(String source) {
        return source != null ? source : "ops-console";
    }
}
