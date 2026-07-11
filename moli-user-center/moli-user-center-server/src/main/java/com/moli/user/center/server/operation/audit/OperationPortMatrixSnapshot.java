package com.moli.user.center.server.operation.audit;

import com.moli.user.center.common.domain.vo.OperationPortMatrixEntryVo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * 不可变端口矩阵快照：匹配 + 审计矩阵列表。
 */
public final class OperationPortMatrixSnapshot {

    private final List<Row> rows;
    private final boolean usingDefaults;

    private OperationPortMatrixSnapshot(List<Row> rows, boolean usingDefaults) {
        this.rows = Collections.unmodifiableList(rows);
        this.usingDefaults = usingDefaults;
    }

    public static OperationPortMatrixSnapshot of(List<Row> rows, boolean usingDefaults) {
        return new OperationPortMatrixSnapshot(rows, usingDefaults);
    }

    public boolean isUsingDefaults() {
        return usingDefaults;
    }

    public OperationPortMatrixPortCheck check(String name, String port) {
        Optional<Row> matched = resolve(name);
        if (!matched.isPresent()) {
            return new OperationPortMatrixPortCheck(OperationPortMatchStatus.UNMAPPED, null, null,
                    "未在端口矩阵中登记");
        }
        Row row = matched.get();
        String actual = OperationPortMatrixNormalizer.normalizePort(port);
        if (actual == null) {
            return new OperationPortMatrixPortCheck(OperationPortMatchStatus.SKIPPED, row.expectedPort,
                    row.matrixKey, "无端口可比对");
        }
        if (actual.equals(row.expectedPort)) {
            return new OperationPortMatrixPortCheck(OperationPortMatchStatus.MATCH, row.expectedPort,
                    row.matrixKey, "与矩阵一致");
        }
        return new OperationPortMatrixPortCheck(OperationPortMatchStatus.MISMATCH, row.expectedPort,
                row.matrixKey, "期望 " + row.expectedPort + "，实际 " + actual);
    }

    public List<OperationPortMatrixEntryVo> auditEntries() {
        List<OperationPortMatrixEntryVo> list = new ArrayList<>(rows.size());
        for (Row row : rows) {
            OperationPortMatrixEntryVo vo = new OperationPortMatrixEntryVo();
            vo.setKey(row.matrixKey);
            vo.setExpectedPort(row.expectedPort);
            vo.setSource(row.source);
            list.add(vo);
        }
        return list;
    }

    public Optional<Row> resolve(String name) {
        if (StringUtils.isBlank(name)) {
            return Optional.empty();
        }
        String normalized = OperationPortMatrixNormalizer.normalizeName(name);
        for (Row row : rows) {
            if (row.matches(normalized)) {
                return Optional.of(row);
            }
        }
        return Optional.empty();
    }

    public static final class Row {
        public final String matrixKey;
        public final String expectedPort;
        public final String source;
        public final int sortOrder;
        private final Set<String> matchNames;

        public Row(String matrixKey, String expectedPort, String source, int sortOrder, List<String> aliases) {
            this.matrixKey = matrixKey;
            this.expectedPort = expectedPort;
            this.source = source;
            this.sortOrder = sortOrder;
            this.matchNames = new LinkedHashSet<>();
            this.matchNames.add(normalizeKey(matrixKey));
            if (aliases != null) {
                for (String alias : aliases) {
                    if (StringUtils.isNotBlank(alias)) {
                        this.matchNames.add(normalizeKey(alias));
                    }
                }
            }
        }

        boolean matches(String normalizedName) {
            return matchNames.contains(normalizedName);
        }

        private static String normalizeKey(String token) {
            return token.trim().toLowerCase(Locale.ROOT).replace('_', '-');
        }
    }
}
