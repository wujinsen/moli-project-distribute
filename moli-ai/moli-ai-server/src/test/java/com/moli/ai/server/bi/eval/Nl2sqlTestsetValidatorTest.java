package com.moli.ai.server.bi.eval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moli.ai.server.bi.config.BiChatProperties;
import com.moli.ai.server.bi.security.BiSqlRejectCode;
import com.moli.ai.server.bi.security.BiSqlSecurityValidator;
import com.moli.ai.server.bi.security.BiSqlValidationResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * W8 · nl2sql_testset.jsonl validator 段 100% 拒答（离线可跑）。
 */
public class Nl2sqlTestsetValidatorTest {

    private static final Path TESTSET = Paths.get("bi/eval/nl2sql_testset.jsonl");

    private BiSqlSecurityValidator validator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        BiChatProperties props = new BiChatProperties();
        props.setAllowTables(Arrays.asList("seckill_order", "seckill_activity"));
        validator = new BiSqlSecurityValidator(props);
    }

    @Test
    public void testsetValidatorCasesMustReject() throws Exception {
        Assert.assertTrue("missing testset: " + TESTSET, Files.isRegularFile(TESTSET));
        List<String> failures = new ArrayList<>();
        int count = 0;
        try (BufferedReader reader = Files.newBufferedReader(TESTSET, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                JsonNode node = objectMapper.readTree(line);
                if (!"validator".equals(node.path("mode").asText())) {
                    continue;
                }
                count++;
                String id = node.path("id").asText("?");
                String sql = node.path("sql").asText();
                String expect = node.path("expect_reject").asText();
                BiSqlValidationResult result = validator.validate(sql);
                if (result.isPassed()) {
                    failures.add(id + " should reject but passed: " + sql);
                    continue;
                }
                BiSqlRejectCode code = result.getRejectCode();
                if (code == null || !expect.equals(code.getCode())) {
                    failures.add(id + " expect " + expect + " got "
                            + (code == null ? "null" : code.getCode()));
                }
            }
        }
        Assert.assertTrue("no validator cases in testset", count > 0);
        if (!failures.isEmpty()) {
            Assert.fail(String.join("\n", failures));
        }
    }
}
