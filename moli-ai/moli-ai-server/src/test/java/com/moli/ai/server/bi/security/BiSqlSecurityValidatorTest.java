package com.moli.ai.server.bi.security;

import com.moli.ai.server.bi.config.BiChatProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * §4.2 危险 SQL 100% 拦截 · W5 出口标准。
 */
@RunWith(Parameterized.class)
public class BiSqlSecurityValidatorTest {

    private final String sql;
    private final BiSqlRejectCode expectedCode;

    private BiSqlSecurityValidator validator;

    public BiSqlSecurityValidatorTest(String sql, BiSqlRejectCode expectedCode) {
        this.sql = sql;
        this.expectedCode = expectedCode;
    }

    @Before
    public void setUp() {
        BiChatProperties props = new BiChatProperties();
        props.setAllowTables(Arrays.asList("seckill_order", "seckill_activity"));
        validator = new BiSqlSecurityValidator(props);
    }

    @Parameterized.Parameters(name = "{1}: {0}")
    public static Collection<Object[]> dangerousSql() {
        return Arrays.asList(new Object[][]{
                {"UPDATE seckill_order SET status=1", BiSqlRejectCode.REJECT_NON_SELECT},
                {"DELETE FROM seckill_order", BiSqlRejectCode.REJECT_NON_SELECT},
                {"INSERT INTO seckill_order(id) VALUES(1)", BiSqlRejectCode.REJECT_NON_SELECT},
                {"DROP TABLE seckill_order", BiSqlRejectCode.REJECT_NON_SELECT},
                {"SELECT id FROM seckill_order; DROP TABLE seckill_order", BiSqlRejectCode.REJECT_MULTI_STATEMENT},
                {"SELECT * FROM seckill_order", BiSqlRejectCode.REJECT_STAR_SELECT},
                {"SELECT o.* FROM seckill_order o", BiSqlRejectCode.REJECT_STAR_SELECT},
                {"SELECT id FROM sys_user", BiSqlRejectCode.REJECT_TABLE_NOT_ALLOWED},
                {"SELECT id FROM seckill_order WHERE id IN (SELECT user_id FROM sys_user)", BiSqlRejectCode.REJECT_TABLE_NOT_ALLOWED},
                {"WITH t AS (SELECT id FROM sys_user) SELECT id FROM seckill_order WHERE 1=1", BiSqlRejectCode.REJECT_TABLE_NOT_ALLOWED},
                {"SELECT password FROM seckill_order", BiSqlRejectCode.REJECT_COLUMN_BLOCKED},
                {"SELECT id FROM seckill_order INTO OUTFILE '/tmp/x.txt'", BiSqlRejectCode.REJECT_DANGEROUS},
                {"SELECT LOAD_FILE('/etc/passwd')", BiSqlRejectCode.REJECT_DANGEROUS},
                {"SELECT BENCHMARK(1000000,1)", BiSqlRejectCode.REJECT_DANGEROUS},
                {"SELECT SLEEP(5)", BiSqlRejectCode.REJECT_DANGEROUS},
                {"SELECT GET_LOCK('x',1)", BiSqlRejectCode.REJECT_DANGEROUS},
                {"SELECT id FROM seckill_order ORDER BY SLEEP(5)", BiSqlRejectCode.REJECT_DANGEROUS},
                {"SELECT id FROM seckill_order GROUP BY BENCHMARK(1000000, MD5('x'))", BiSqlRejectCode.REJECT_DANGEROUS},
                {"SELECT CASE WHEN SLEEP(5) > 0 THEN 1 ELSE 0 END FROM seckill_order", BiSqlRejectCode.REJECT_DANGEROUS},
                {"SELECT id FROM seckill_order WHERE id IN (SELECT SLEEP(5))", BiSqlRejectCode.REJECT_DANGEROUS},
                {"SELECT id FROM seckill_order ORDER BY password", BiSqlRejectCode.REJECT_COLUMN_BLOCKED},
                {"SELECT (CASE WHEN 1=1 THEN password END) FROM seckill_order", BiSqlRejectCode.REJECT_COLUMN_BLOCKED},
                {"WITH RECURSIVE r AS (SELECT 1 n UNION SELECT n+1 FROM r WHERE n<5) SELECT * FROM r", BiSqlRejectCode.REJECT_DANGEROUS},
                {"SELECT id FROM information_schema.tables", BiSqlRejectCode.REJECT_DANGEROUS},
                {"SELECT id FROM seckill_order UNION SELECT password FROM sys_user", BiSqlRejectCode.REJECT_TABLE_NOT_ALLOWED},
                {"not valid sql at all", BiSqlRejectCode.REJECT_DANGEROUS},
                {"SELECT @v := 1", BiSqlRejectCode.REJECT_DANGEROUS},
        });
    }

    @Test
    public void dangerousSqlMustBeRejected() {
        BiSqlValidationResult result = validator.validate(sql);
        Assert.assertFalse("should reject: " + sql, result.isPassed());
        Assert.assertEquals(expectedCode, result.getRejectCode());
    }
}
