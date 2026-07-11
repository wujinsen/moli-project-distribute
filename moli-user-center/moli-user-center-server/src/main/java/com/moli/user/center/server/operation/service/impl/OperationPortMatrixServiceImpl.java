package com.moli.user.center.server.operation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.moli.common.page.PageRes;
import com.moli.user.center.common.domain.dto.operation.OperationPortMatrixSaveRequest;
import com.moli.user.center.common.domain.entity.OperationPortMatrixAliasInfo;
import com.moli.user.center.common.domain.entity.OperationPortMatrixInfo;
import com.moli.user.center.common.domain.vo.OperationPortMatrixVo;
import com.moli.user.center.server.operation.audit.OperationPortMatrixNormalizer;
import com.moli.user.center.server.operation.audit.OperationPortMatrixProvider;
import com.moli.user.center.server.operation.mapper.OperationPortMatrixAliasMapper;
import com.moli.user.center.server.operation.mapper.OperationPortMatrixMapper;
import com.moli.user.center.server.operation.service.OperationPortMatrixService;
import com.moli.user.center.server.operation.support.OperationBizException;
import com.moli.user.center.server.operation.support.OperationCrudSupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OperationPortMatrixServiceImpl implements OperationPortMatrixService {

    private static final int MAX_ALIASES = 32;

    @Resource
    private OperationPortMatrixMapper operationPortMatrixMapper;
    @Resource
    private OperationPortMatrixAliasMapper operationPortMatrixAliasMapper;
    @Resource
    private OperationCrudSupport crudSupport;
    @Resource
    private OperationPortMatrixProvider portMatrixProvider;

    @Override
    public PageRes<OperationPortMatrixVo> list(OperationPortMatrixInfo query) {
        LambdaQueryWrapper<OperationPortMatrixInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(query.getMatrixKey())) {
            wrapper.like(OperationPortMatrixInfo::getMatrixKey, query.getMatrixKey());
        }
        if (StringUtils.isNotBlank(query.getDisplayName())) {
            wrapper.like(OperationPortMatrixInfo::getDisplayName, query.getDisplayName());
        }
        if (query.getEnabled() != null) {
            wrapper.eq(OperationPortMatrixInfo::getEnabled, query.getEnabled());
        }
        wrapper.orderByAsc(OperationPortMatrixInfo::getSortOrder)
                .orderByAsc(OperationPortMatrixInfo::getMatrixKey);
        PageRes<OperationPortMatrixVo> page = crudSupport.selectPage(operationPortMatrixMapper, wrapper,
                query.getPageNum(), query.getPageSize(), this::toVoWithoutAliases);
        attachAliases(page.getList());
        return page;
    }

    @Override
    public OperationPortMatrixVo getById(Long id) {
        OperationPortMatrixVo vo = toVoWithoutAliases(requireRow(id));
        attachAliases(Collections.singletonList(vo));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(OperationPortMatrixSaveRequest request) {
        String matrixKey = request.getMatrixKey().trim();
        validateExpectedPort(request.getExpectedPort());
        assertMatrixKeyAvailable(matrixKey, null);
        List<String> aliases = normalizeAliases(request.getAliases(), matrixKey);
        assertAliasesAvailable(matrixKey, aliases, null);

        OperationPortMatrixInfo row = toEntity(request, matrixKey);
        row.setEnabled(request.getEnabled() != null ? request.getEnabled() : Boolean.TRUE);
        row.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        row.setSource(StringUtils.defaultIfBlank(request.getSource(), "ops-console"));
        operationPortMatrixMapper.insert(row);
        replaceAliases(row.getId(), aliases);
        portMatrixProvider.refresh();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(OperationPortMatrixSaveRequest request) {
        crudSupport.assertUpdateId(request.getId());
        OperationPortMatrixInfo existing = requireRow(request.getId());
        String matrixKey = request.getMatrixKey().trim();
        if (!matrixKey.equals(existing.getMatrixKey())) {
            throw OperationBizException.params("matrixKey 不可修改");
        }
        validateExpectedPort(request.getExpectedPort());
        List<String> aliases = normalizeAliases(request.getAliases(), matrixKey);
        assertAliasesAvailable(matrixKey, aliases, existing.getId());

        OperationPortMatrixInfo row = toEntity(request, matrixKey);
        row.setId(existing.getId());
        if (request.getEnabled() == null) {
            row.setEnabled(existing.getEnabled());
        }
        if (request.getSortOrder() == null) {
            row.setSortOrder(existing.getSortOrder());
        }
        if (StringUtils.isBlank(request.getSource())) {
            row.setSource(existing.getSource());
        }
        operationPortMatrixMapper.updateById(row);
        replaceAliases(existing.getId(), aliases);
        portMatrixProvider.refresh();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(Long[] ids) {
        crudSupport.deleteEach(ids, id -> {
            operationPortMatrixAliasMapper.delete(new LambdaQueryWrapper<OperationPortMatrixAliasInfo>()
                    .eq(OperationPortMatrixAliasInfo::getMatrixId, id));
        }, operationPortMatrixMapper::deleteById);
        portMatrixProvider.refresh();
    }

    private OperationPortMatrixInfo requireRow(Long id) {
        return crudSupport.requireRow(operationPortMatrixMapper, id, "端口矩阵");
    }

    private OperationPortMatrixVo toVoWithoutAliases(OperationPortMatrixInfo row) {
        OperationPortMatrixVo vo = new OperationPortMatrixVo();
        BeanUtils.copyProperties(row, vo);
        vo.setUsingDefaults(Boolean.FALSE);
        return vo;
    }

    private void attachAliases(List<OperationPortMatrixVo> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        List<Long> ids = rows.stream().map(OperationPortMatrixVo::getId).collect(Collectors.toList());
        List<OperationPortMatrixAliasInfo> aliasRows = operationPortMatrixAliasMapper.selectList(
                new LambdaQueryWrapper<OperationPortMatrixAliasInfo>()
                        .in(OperationPortMatrixAliasInfo::getMatrixId, ids)
                        .orderByAsc(OperationPortMatrixAliasInfo::getAlias));
        Map<Long, List<String>> grouped = aliasRows.stream()
                .collect(Collectors.groupingBy(OperationPortMatrixAliasInfo::getMatrixId,
                        Collectors.mapping(OperationPortMatrixAliasInfo::getAlias, Collectors.toList())));
        for (OperationPortMatrixVo row : rows) {
            row.setAliases(grouped.getOrDefault(row.getId(), Collections.emptyList()));
        }
    }

    private OperationPortMatrixInfo toEntity(OperationPortMatrixSaveRequest request, String matrixKey) {
        OperationPortMatrixInfo row = new OperationPortMatrixInfo();
        row.setMatrixKey(matrixKey);
        row.setDisplayName(request.getDisplayName());
        row.setExpectedPort(request.getExpectedPort().trim());
        row.setSortOrder(request.getSortOrder());
        row.setEnabled(request.getEnabled());
        row.setSource(request.getSource());
        row.setRemark(request.getRemark());
        return row;
    }

    private void replaceAliases(Long matrixId, List<String> aliases) {
        operationPortMatrixAliasMapper.delete(new LambdaQueryWrapper<OperationPortMatrixAliasInfo>()
                .eq(OperationPortMatrixAliasInfo::getMatrixId, matrixId));
        if (aliases.isEmpty()) {
            return;
        }
        Date now = new Date();
        for (String alias : aliases) {
            OperationPortMatrixAliasInfo row = new OperationPortMatrixAliasInfo();
            row.setMatrixId(matrixId);
            row.setAlias(alias);
            row.setCreateTime(now);
            operationPortMatrixAliasMapper.insert(row);
        }
    }

    private static List<String> normalizeAliases(List<String> aliases, String matrixKey) {
        if (aliases == null || aliases.isEmpty()) {
            return Collections.emptyList();
        }
        if (aliases.size() > MAX_ALIASES) {
            throw OperationBizException.params("aliases 最多 " + MAX_ALIASES + " 个");
        }
        Set<String> normalized = new LinkedHashSet<>();
        String keyNorm = OperationPortMatrixNormalizer.normalizeAliasToken(matrixKey);
        for (String alias : aliases) {
            if (StringUtils.isBlank(alias)) {
                continue;
            }
            String token = OperationPortMatrixNormalizer.normalizeAliasToken(alias);
            if (!token.matches("^[a-z][a-z0-9-]{0,127}$")) {
                throw OperationBizException.params("别名格式非法: " + alias);
            }
            if (token.equals(keyNorm)) {
                continue;
            }
            normalized.add(token);
        }
        return new ArrayList<>(normalized);
    }

    private void validateExpectedPort(String expectedPort) {
        if (StringUtils.isBlank(expectedPort)) {
            throw OperationBizException.params("expectedPort 不能为空");
        }
        try {
            int port = Integer.parseInt(expectedPort.trim());
            if (port < 1 || port > 65535) {
                throw OperationBizException.params("expectedPort 须在 1..65535");
            }
        } catch (NumberFormatException ex) {
            throw OperationBizException.params("expectedPort 须在 1..65535");
        }
    }

    private void assertMatrixKeyAvailable(String matrixKey, Long excludeMatrixId) {
        LambdaQueryWrapper<OperationPortMatrixInfo> keyQuery = new LambdaQueryWrapper<>();
        keyQuery.eq(OperationPortMatrixInfo::getMatrixKey, matrixKey);
        if (excludeMatrixId != null) {
            keyQuery.ne(OperationPortMatrixInfo::getId, excludeMatrixId);
        }
        if (operationPortMatrixMapper.selectCount(keyQuery) > 0) {
            throw OperationBizException.params("matrixKey 已存在：" + matrixKey);
        }
        assertTokenNotUsedAsAlias(matrixKey, excludeMatrixId);
    }

    private void assertAliasesAvailable(String matrixKey, List<String> aliases, Long excludeMatrixId) {
        Set<String> reserved = loadReservedTokens(excludeMatrixId);
        reserved.add(OperationPortMatrixNormalizer.normalizeAliasToken(matrixKey));
        for (String alias : aliases) {
            if (reserved.contains(alias)) {
                throw OperationBizException.params("别名已被占用：" + alias);
            }
            reserved.add(alias);
        }
    }

    private void assertTokenNotUsedAsAlias(String token, Long excludeMatrixId) {
        LambdaQueryWrapper<OperationPortMatrixAliasInfo> query = new LambdaQueryWrapper<>();
        query.eq(OperationPortMatrixAliasInfo::getAlias, OperationPortMatrixNormalizer.normalizeAliasToken(token));
        if (excludeMatrixId != null) {
            query.ne(OperationPortMatrixAliasInfo::getMatrixId, excludeMatrixId);
        }
        if (operationPortMatrixAliasMapper.selectCount(query) > 0) {
            throw OperationBizException.params("别名已被占用：" + token);
        }
    }

    private Set<String> loadReservedTokens(Long excludeMatrixId) {
        Set<String> tokens = new HashSet<>();
        List<OperationPortMatrixInfo> matrices = operationPortMatrixMapper.selectList(null);
        for (OperationPortMatrixInfo matrix : matrices) {
            if (excludeMatrixId != null && excludeMatrixId.equals(matrix.getId())) {
                continue;
            }
            tokens.add(OperationPortMatrixNormalizer.normalizeAliasToken(matrix.getMatrixKey()));
        }
        List<OperationPortMatrixAliasInfo> aliases = operationPortMatrixAliasMapper.selectList(null);
        for (OperationPortMatrixAliasInfo alias : aliases) {
            if (excludeMatrixId != null && excludeMatrixId.equals(alias.getMatrixId())) {
                continue;
            }
            tokens.add(alias.getAlias());
        }
        return tokens;
    }
}
