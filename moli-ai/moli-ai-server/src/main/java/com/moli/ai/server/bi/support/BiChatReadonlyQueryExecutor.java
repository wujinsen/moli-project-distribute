package com.moli.ai.server.bi.support;

import com.moli.ai.server.bi.config.BiChatProperties;
import com.moli.ai.server.bi.config.BiChatReadonlyDataSourceConfig;
import com.moli.ai.server.bi.dto.BiColumnVo;
import com.moli.ai.server.bi.enums.BiChatResponseCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 只读 SQL 执行（INV-2 · Java 唯一执行者）。
 */
@Component
public class BiChatReadonlyQueryExecutor {

    private final JdbcTemplate jdbcTemplate;
    private final BiChatProperties properties;

    public BiChatReadonlyQueryExecutor(
            @Qualifier(BiChatReadonlyDataSourceConfig.READONLY_JDBC_TEMPLATE) JdbcTemplate jdbcTemplate,
            BiChatProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    public BiQueryResult execute(String sql, Integer userMaxRows) throws BiQueryExecutionException {
        int fetchLimit = resolveFetchLimit(userMaxRows);
        int scanCap = Math.max(1, properties.getMaxScanRows());
        int timeoutSec = (int) Math.max(1L, properties.getQueryTimeoutMs() / 1000L);
        jdbcTemplate.setQueryTimeout(timeoutSec);

        try {
            return jdbcTemplate.query(sql, rs -> {
                ResultSetMetaData meta = rs.getMetaData();
                List<BiColumnVo> columns = buildColumns(meta);
                List<Map<String, Object>> rows = new ArrayList<>();
                int count = 0;
                while (rs.next()) {
                    count++;
                    if (count > scanCap || count > fetchLimit) {
                        throw new RowsExceededSignal();
                    }
                    rows.add(mapRow(rs, meta));
                }
                BiQueryResult result = new BiQueryResult();
                result.setColumns(columns);
                result.setRows(rows);
                result.setRowCount(count);
                return result;
            });
        } catch (QueryTimeoutException ex) {
            throw new BiQueryExecutionException(BiChatResponseCode.BI_SQL_EXEC_TIMEOUT, "query timeout");
        } catch (RowsExceededSignal ex) {
            throw new BiQueryExecutionException(BiChatResponseCode.BI_SQL_EXEC_ROWS_EXCEEDED,
                    "result rows exceeded");
        } catch (DataAccessException ex) {
            if (ex.getCause() instanceof RowsExceededSignal) {
                throw new BiQueryExecutionException(BiChatResponseCode.BI_SQL_EXEC_ROWS_EXCEEDED,
                        "result rows exceeded");
            }
            throw new BiQueryExecutionException(BiChatResponseCode.BI_SQL_EXEC_ERROR, "sql execution failed");
        }
    }

    private int resolveFetchLimit(Integer userMaxRows) {
        int cap = Math.max(1, properties.getMaxRows());
        if (userMaxRows != null && userMaxRows > 0) {
            cap = Math.min(cap, userMaxRows);
        }
        return cap;
    }

    private static List<BiColumnVo> buildColumns(ResultSetMetaData meta) throws SQLException {
        List<BiColumnVo> columns = new ArrayList<>();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            BiColumnVo col = new BiColumnVo();
            col.setName(meta.getColumnLabel(i));
            col.setType(meta.getColumnTypeName(i));
            col.setLabel(meta.getColumnLabel(i));
            columns.add(col);
        }
        return columns;
    }

    private static Map<String, Object> mapRow(ResultSet rs, ResultSetMetaData meta) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            String label = meta.getColumnLabel(i);
            Object value = rs.getObject(i);
            if (value instanceof Timestamp) {
                value = value.toString();
            }
            row.put(label, value);
        }
        return row;
    }

    private static final class RowsExceededSignal extends RuntimeException {
    }
}
