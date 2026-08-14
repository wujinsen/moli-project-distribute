package com.moli.ai.server.bi.security;

import com.moli.ai.server.bi.config.BiChatProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class BiSqlSecurityValidatorPolicyTest {

    private BiSqlSecurityValidator validator;

    @Before
    public void setUp() {
        BiChatProperties props = new BiChatProperties();
        props.setAllowTables(Arrays.asList("seckill_order", "seckill_activity"));
        validator = new BiSqlSecurityValidator(props);
    }

    @Test
    public void emptyAllowListDenyAll() {
        BiChatProperties empty = new BiChatProperties();
        BiSqlSecurityValidator denyAll = new BiSqlSecurityValidator(empty);
        BiSqlValidationResult result = denyAll.validate("SELECT id FROM seckill_order LIMIT 1");
        Assert.assertFalse(result.isPassed());
        Assert.assertEquals(BiSqlRejectCode.REJECT_TABLE_NOT_ALLOWED, result.getRejectCode());
    }

    @Test
    public void safeSelectPassesWithLimitInjection() {
        BiSqlValidationResult result = validator.validate("SELECT id FROM seckill_order");
        Assert.assertTrue(result.isPassed());
        Assert.assertNotNull(result.getSanitizedSql());
        Assert.assertTrue(result.getSanitizedSql().toUpperCase().contains("LIMIT"));
    }

    @Test
    public void explicitLimitWithinMaxPasses() {
        BiSqlValidationResult result = validator.validate("SELECT id FROM seckill_order LIMIT 10");
        Assert.assertTrue(result.isPassed());
    }

    @Test
    public void inSubquerySleepIsRejected() {
        BiSqlValidationResult result = validator.validate(
                "SELECT id FROM seckill_order WHERE id IN (SELECT SLEEP(5))");
        Assert.assertFalse(result.isPassed());
        Assert.assertEquals(BiSqlRejectCode.REJECT_DANGEROUS, result.getRejectCode());
    }
}
