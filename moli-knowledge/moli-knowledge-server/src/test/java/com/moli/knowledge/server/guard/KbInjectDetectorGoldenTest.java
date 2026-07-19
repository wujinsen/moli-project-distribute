package com.moli.knowledge.server.guard;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 金样 guardrails_inject.jsonl：BLOCK 100%、PASS 零 BLOCK。
 */
public class KbInjectDetectorGoldenTest {

    private final KbInjectDetector detector = new KbInjectDetector();

    @Test
    public void goldenJsonl_blockAndPassExpectations() throws Exception {
        Path jsonl = Paths.get("..", "kb", "eval", "guardrails_inject.jsonl").normalize();
        if (!Files.exists(jsonl)) {
            jsonl = Paths.get("..", "..", "kb", "eval", "guardrails_inject.jsonl").normalize();
        }
        Assert.assertTrue("missing " + jsonl, Files.exists(jsonl));

        List<String> lines = Files.readAllLines(jsonl, StandardCharsets.UTF_8);
        int blockChecked = 0;
        int passChecked = 0;
        for (String line : lines) {
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            JSONObject row = JSON.parseObject(line);
            String expect = row.getString("expect");
            String text = row.getString("text");
            KbInjectDetector.InjectDetectResult result = detector.detect(text);
            if ("BLOCK".equals(expect)) {
                Assert.assertEquals("BLOCK sample failed: " + row.getString("id"),
                        InjectSeverity.BLOCK, result.getSeverity());
                blockChecked++;
            } else if ("PASS".equals(expect)) {
                Assert.assertNotEquals("PASS sample blocked: " + row.getString("id"),
                        InjectSeverity.BLOCK, result.getSeverity());
                passChecked++;
            }
        }
        Assert.assertTrue(blockChecked >= 20);
        Assert.assertTrue(passChecked >= 20);
    }
}
