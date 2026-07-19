package com.moli.ai.server.bi.security;

import com.moli.ai.server.bi.config.BiChatProperties;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.VariableAssignment;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.GroupByElement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * §3.2 SQL 白名单校验（JSqlParser 4.9 AST · fail-closed）。
 * 表达式遍历使用 {@link ExpressionVisitorAdapter} 覆盖全部子句（含 GROUP BY / ORDER BY）。
 */
@Component
public class BiSqlSecurityValidator {

    private static final Set<String> SYSTEM_SCHEMAS = new HashSet<>(Arrays.asList(
            "mysql", "information_schema", "performance_schema", "sys"
    ));

    private static final Set<String> DANGEROUS_FUNCTIONS = new HashSet<>(Arrays.asList(
            "LOAD_FILE", "BENCHMARK", "SLEEP", "GET_LOCK"
    ));

    private final BiChatProperties properties;

    public BiSqlSecurityValidator(BiChatProperties properties) {
        this.properties = properties;
    }

    public BiSqlValidationResult validate(String sql) {
        if (!StringUtils.hasText(sql)) {
            return BiSqlValidationResult.reject(BiSqlRejectCode.REJECT_DANGEROUS, "empty sql");
        }
        if (properties.getAllowTables() == null || properties.getAllowTables().isEmpty()) {
            return BiSqlValidationResult.reject(BiSqlRejectCode.REJECT_TABLE_NOT_ALLOWED,
                    "allow-tables not configured (deny-all)");
        }

        final Select select;
        try {
            Statements statements = CCJSqlParserUtil.parseStatements(sql.trim());
            if (statements == null || statements.getStatements() == null || statements.getStatements().size() != 1) {
                return BiSqlValidationResult.reject(BiSqlRejectCode.REJECT_MULTI_STATEMENT,
                        "multiple statements are not allowed");
            }
            Statement stmt = statements.getStatements().get(0);
            if (!(stmt instanceof Select)) {
                return BiSqlValidationResult.reject(BiSqlRejectCode.REJECT_NON_SELECT,
                        "only SELECT is allowed");
            }
            select = (Select) stmt;
        } catch (JSQLParserException e) {
            return BiSqlValidationResult.reject(BiSqlRejectCode.REJECT_DANGEROUS, "sql parse failed");
        }

        Set<String> allowTables = normalizeTableSet(properties.getAllowTables());
        AstScan scan = new AstScan();
        try {
            scanInspectSelect(select, scan);
        } catch (SecurityRejectException ex) {
            return BiSqlValidationResult.reject(ex.code, ex.reason);
        } catch (Exception ex) {
            return BiSqlValidationResult.reject(BiSqlRejectCode.REJECT_DANGEROUS,
                    "unsupported sql structure");
        }

        List<String> referencedTables = new TablesNamesFinder().getTableList((Statement) select);
        for (String raw : referencedTables) {
            TableRef ref = parseTableRef(raw);
            if (ref.schema != null && SYSTEM_SCHEMAS.contains(ref.schema)) {
                return BiSqlValidationResult.reject(BiSqlRejectCode.REJECT_DANGEROUS,
                        "system schema access is forbidden");
            }
            if (!allowTables.contains(ref.table)) {
                return BiSqlValidationResult.reject(BiSqlRejectCode.REJECT_TABLE_NOT_ALLOWED,
                        "table not in allow-list: " + ref.table);
            }
        }

        if (scan.starSelect) {
            return BiSqlValidationResult.reject(BiSqlRejectCode.REJECT_STAR_SELECT,
                    "SELECT * / t.* is not allowed");
        }
        if (scan.blockedColumn != null) {
            return BiSqlValidationResult.reject(BiSqlRejectCode.REJECT_COLUMN_BLOCKED,
                    "column blocked: " + scan.blockedColumn);
        }

        try {
            applyLimitPolicy(select);
            return BiSqlValidationResult.pass(select.toString());
        } catch (Exception ex) {
            return BiSqlValidationResult.reject(BiSqlRejectCode.REJECT_DANGEROUS, "limit policy failed");
        }
    }

    private void scanInspectSelect(Select select, AstScan scan) {
        if (select.getWithItemsList() != null) {
            for (WithItem withItem : select.getWithItemsList()) {
                if (withItem.isRecursive()) {
                    throw new SecurityRejectException(BiSqlRejectCode.REJECT_DANGEROUS,
                            "WITH RECURSIVE is forbidden");
                }
                if (withItem.getSelect() != null) {
                    scanSelect(withItem.getSelect(), scan);
                }
            }
        }
        scanSelect(select, scan);
    }

    private void scanSelect(Select select, AstScan scan) {
        if (select instanceof PlainSelect) {
            scanPlainSelect((PlainSelect) select, scan);
        } else if (select instanceof SetOperationList) {
            for (Select part : ((SetOperationList) select).getSelects()) {
                scanSelect(part, scan);
            }
        } else if (select instanceof ParenthesedSelect) {
            Select inner = ((ParenthesedSelect) select).getSelect();
            if (inner != null) {
                scanSelect(inner, scan);
            }
        } else {
            throw new SecurityRejectException(BiSqlRejectCode.REJECT_DANGEROUS, "unrecognized select");
        }
    }

    private void scanPlainSelect(PlainSelect plain, AstScan scan) {
        if (plain.getIntoTables() != null && !plain.getIntoTables().isEmpty()) {
            throw new SecurityRejectException(BiSqlRejectCode.REJECT_DANGEROUS, "INTO is forbidden");
        }
        if (plain.getSelectItems() != null) {
            for (SelectItem<?> item : plain.getSelectItems()) {
                acceptExpression(item.getExpression(), scan);
            }
        }
        if (plain.getFromItem() != null) {
            scanFromItem(plain.getFromItem(), scan);
        }
        if (plain.getJoins() != null) {
            for (Join join : plain.getJoins()) {
                if (join.getRightItem() != null) {
                    scanFromItem(join.getRightItem(), scan);
                }
                acceptExpression(join.getOnExpression(), scan);
                if (join.getOnExpressions() != null) {
                    for (Expression on : join.getOnExpressions()) {
                        acceptExpression(on, scan);
                    }
                }
            }
        }
        acceptExpression(plain.getWhere(), scan);
        acceptExpression(plain.getHaving(), scan);
        scanGroupBy(plain.getGroupBy(), scan);
        if (plain.getOrderByElements() != null) {
            for (OrderByElement orderBy : plain.getOrderByElements()) {
                acceptExpression(orderBy.getExpression(), scan);
            }
        }
    }

    private void scanGroupBy(GroupByElement groupBy, AstScan scan) {
        if (groupBy == null) {
            return;
        }
        if (groupBy.getGroupByExpressionList() != null
                && groupBy.getGroupByExpressionList().getExpressions() != null) {
            for (Object raw : groupBy.getGroupByExpressionList().getExpressions()) {
                if (raw instanceof Expression) {
                    acceptExpression((Expression) raw, scan);
                } else {
                    throw new SecurityRejectException(BiSqlRejectCode.REJECT_DANGEROUS,
                            "unsupported group by expression");
                }
            }
        }
        if (groupBy.getGroupingSets() != null) {
            for (net.sf.jsqlparser.expression.operators.relational.ExpressionList<?> groupingSet
                    : groupBy.getGroupingSets()) {
                if (groupingSet.getExpressions() != null) {
                    for (Object raw : groupingSet.getExpressions()) {
                        if (raw instanceof Expression) {
                            acceptExpression((Expression) raw, scan);
                        } else {
                            throw new SecurityRejectException(BiSqlRejectCode.REJECT_DANGEROUS,
                                    "unsupported grouping set expression");
                        }
                    }
                }
            }
        }
    }

    private void scanFromItem(FromItem fromItem, AstScan scan) {
        if (fromItem instanceof ParenthesedSelect) {
            Select inner = ((ParenthesedSelect) fromItem).getSelect();
            if (inner != null) {
                scanSelect(inner, scan);
            }
        } else if (fromItem instanceof LateralSubSelect) {
            Select inner = ((LateralSubSelect) fromItem).getSelect();
            if (inner != null) {
                scanSelect(inner, scan);
            }
        } else if (fromItem instanceof Table) {
            return;
        } else {
            throw new SecurityRejectException(BiSqlRejectCode.REJECT_DANGEROUS, "unrecognized from item");
        }
    }

    private void acceptExpression(Expression expression, AstScan scan) {
        if (expression == null) {
            return;
        }
        expression.accept(new SecurityExpressionVisitor(scan));
    }

    private final class SecurityExpressionVisitor extends ExpressionVisitorAdapter {

        private final AstScan scan;

        private SecurityExpressionVisitor(AstScan scan) {
            this.scan = scan;
        }

        @Override
        public void visit(AllColumns allColumns) {
            scan.starSelect = true;
        }

        @Override
        public void visit(AllTableColumns allTableColumns) {
            scan.starSelect = true;
        }

        @Override
        public void visit(Column column) {
            checkColumn(column, scan);
            super.visit(column);
        }

        @Override
        public void visit(Function function) {
            String name = function.getName() == null ? "" : function.getName().toUpperCase(Locale.ROOT);
            if (DANGEROUS_FUNCTIONS.contains(name)) {
                throw new SecurityRejectException(BiSqlRejectCode.REJECT_DANGEROUS,
                        "dangerous function: " + name);
            }
            super.visit(function);
        }

        @Override
        public void visit(UserVariable var) {
            throw new SecurityRejectException(BiSqlRejectCode.REJECT_DANGEROUS, "user variables are forbidden");
        }

        @Override
        public void visit(VariableAssignment var) {
            throw new SecurityRejectException(BiSqlRejectCode.REJECT_DANGEROUS,
                    "assignment operator is forbidden");
        }

        @Override
        public void visit(ParenthesedSelect select) {
            if (select.getSelect() != null) {
                scanSelect(select.getSelect(), scan);
            }
        }

        @Override
        public void visit(Select select) {
            scanSelect(select, scan);
        }

        @Override
        public void visit(InExpression inExpression) {
            if (inExpression.toString().contains(":=")) {
                throw new SecurityRejectException(BiSqlRejectCode.REJECT_DANGEROUS,
                        "assignment operator is forbidden");
            }
            super.visit(inExpression);
        }

        @Override
        protected void visitBinaryExpression(BinaryExpression binaryExpression) {
            if (binaryExpression.toString().contains(":=")) {
                throw new SecurityRejectException(BiSqlRejectCode.REJECT_DANGEROUS,
                        "assignment operator is forbidden");
            }
            super.visitBinaryExpression(binaryExpression);
        }
    }

    private void checkColumn(Column column, AstScan scan) {
        String name = column.getColumnName();
        if (!StringUtils.hasText(name)) {
            return;
        }
        name = stripQuotes(name).toLowerCase(Locale.ROOT);
        if (isDeniedColumn(name)) {
            scan.blockedColumn = name;
        }
    }

    private boolean isDeniedColumn(String columnName) {
        List<String> deny = properties.getDenyColumns();
        if (deny != null) {
            for (String d : deny) {
                if (!StringUtils.hasText(d)) {
                    continue;
                }
                String rule = d.toLowerCase(Locale.ROOT).trim();
                if (rule.endsWith("*")) {
                    if (columnName.startsWith(rule.substring(0, rule.length() - 1))) {
                        return true;
                    }
                } else if (columnName.equals(rule)) {
                    return true;
                }
            }
        }
        return columnName.endsWith("_key");
    }

    private void applyLimitPolicy(Select select) {
        PlainSelect plain = resolvePlainSelect(select);
        if (plain == null) {
            throw new IllegalStateException("no plain select for limit");
        }
        Limit limit = plain.getLimit();
        int maxRows = Math.max(1, properties.getMaxRows());
        int defaultRows = Math.max(1, properties.getDefaultRows());
        if (limit == null || limit.getRowCount() == null) {
            Limit injected = new Limit();
            injected.setRowCount(new LongValue(defaultRows));
            plain.setLimit(injected);
            return;
        }
        long requested = parseLimitValue(limit.getRowCount());
        if (requested > maxRows) {
            limit.setRowCount(new LongValue(maxRows));
        }
    }

    private PlainSelect resolvePlainSelect(Select select) {
        PlainSelect plain = select.getPlainSelect();
        if (plain != null) {
            return plain;
        }
        if (select instanceof SetOperationList) {
            Select first = ((SetOperationList) select).getSelect(0);
            return first == null ? null : first.getPlainSelect();
        }
        return null;
    }

    private long parseLimitValue(Expression expr) {
        if (expr instanceof LongValue) {
            return ((LongValue) expr).getValue();
        }
        throw new IllegalStateException("unsupported limit expression");
    }

    private Set<String> normalizeTableSet(List<String> tables) {
        return tables.stream()
                .filter(StringUtils::hasText)
                .map(t -> stripQuotes(t).toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private TableRef parseTableRef(String raw) {
        String normalized = stripQuotes(raw).toLowerCase(Locale.ROOT);
        int dot = normalized.indexOf('.');
        if (dot >= 0) {
            return new TableRef(normalized.substring(0, dot), normalized.substring(dot + 1));
        }
        return new TableRef(null, normalized);
    }

    private static String stripQuotes(String name) {
        if (name == null) {
            return "";
        }
        String n = name.trim();
        if (n.length() >= 2 && ((n.startsWith("`") && n.endsWith("`")) || (n.startsWith("\"") && n.endsWith("\"")))) {
            return n.substring(1, n.length() - 1);
        }
        return n;
    }

    private static final class AstScan {
        boolean starSelect;
        String blockedColumn;
    }

    private static final class TableRef {
        final String schema;
        final String table;

        TableRef(String schema, String table) {
            this.schema = schema;
            this.table = table;
        }
    }

    private static final class SecurityRejectException extends RuntimeException {
        final BiSqlRejectCode code;
        final String reason;

        SecurityRejectException(BiSqlRejectCode code, String reason) {
            super(reason);
            this.code = code;
            this.reason = reason;
        }
    }
}
