package com.moli.knowledge.server.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 读取 {@code kb/eval/baselines.json}（§1.1 单一真相）；Java 只读，不写基线。
 */
@Component
public class KbEvalBaselinesProvider {

    private static final Logger log = LoggerFactory.getLogger(KbEvalBaselinesProvider.class);

    private static final List<String> STRATEGY_ORDER = Collections.unmodifiableList(
            Arrays.asList("ngram", "hybrid", "hybrid-rerank"));

    @Value("${kb.eval.baselines-path:}")
    private String baselinesPath;

    private JsonNode root;
    private Map<String, BigDecimal> baselineHit3ByStrategy = Collections.emptyMap();

    @PostConstruct
    public void load() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode loaded = null;
        if (baselinesPath != null && !baselinesPath.trim().isEmpty()) {
            loaded = readFile(mapper, Paths.get(baselinesPath.trim()));
        }
        if (loaded == null) {
            Path monorepo = Paths.get("kb/eval/baselines.json");
            if (Files.isRegularFile(monorepo)) {
                loaded = readFile(mapper, monorepo);
            }
        }
        if (loaded == null) {
            Path sibling = Paths.get("../kb/eval/baselines.json");
            if (Files.isRegularFile(sibling)) {
                loaded = readFile(mapper, sibling);
            }
        }
        if (loaded == null) {
            try (InputStream in = getClass().getResourceAsStream("/kb/eval/baselines.json")) {
                if (in != null) {
                    loaded = mapper.readTree(in);
                }
            } catch (IOException e) {
                log.warn("classpath baselines.json unreadable: {}", e.getMessage());
            }
        }
        if (loaded == null) {
            log.warn("kb eval baselines.json not found; retrievalQuality baseline fields will be null");
            return;
        }
        root = loaded;
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        JsonNode strategies = root.path("strategies");
        for (String key : STRATEGY_ORDER) {
            JsonNode node = strategies.path(key);
            if (node.isMissingNode() || node.isNull()) {
                continue;
            }
            map.put(key, node.path("hit3").decimalValue());
        }
        baselineHit3ByStrategy = Collections.unmodifiableMap(map);
    }

    private JsonNode readFile(ObjectMapper mapper, Path path) {
        try {
            return mapper.readTree(path.toFile());
        } catch (IOException e) {
            log.warn("Failed to read baselines from {}: {}", path, e.getMessage());
            return null;
        }
    }

    public List<String> strategyKeys() {
        return STRATEGY_ORDER;
    }

    public BigDecimal baselineHit3(String strategy) {
        return baselineHit3ByStrategy.get(strategy);
    }

    public Integer goldenTotalFromBaselines() {
        if (root == null || !root.has("golden_total")) {
            return null;
        }
        return root.path("golden_total").asInt();
    }

    /**
     * AI-3 §1.2 就地判定。基线缺失或 hit3 为空时返回 {@code null}（未判定，不是未通过）。
     */
    public Boolean evaluateGate(String strategy, BigDecimal hit3, Integer errors, String byDifficultyJson) {
        if (root == null || strategy == null || strategy.trim().isEmpty() || hit3 == null) {
            return null;
        }
        JsonNode strat = root.path("strategies").path(strategy);
        if (strat.isMissingNode() || strat.isNull() || !strat.has("hit3")) {
            return null;
        }
        BigDecimal tolerance = strat.has("tolerance") ? strat.path("tolerance").decimalValue() : BigDecimal.ZERO;
        BigDecimal minHit3 = strat.path("hit3").decimalValue().subtract(tolerance);
        if (hit3.compareTo(minHit3) < 0) {
            return false;
        }
        if (errors != null && errors > 0) {
            return false;
        }
        if (strat.has("dirty_hit3") && !strat.path("dirty_hit3").isNull()) {
            BigDecimal dirtyHit3 = dirtyHit3FromJson(byDifficultyJson);
            if (dirtyHit3 == null) {
                dirtyHit3 = BigDecimal.ZERO;
            }
            BigDecimal minDirty = strat.path("dirty_hit3").decimalValue().subtract(tolerance);
            if (dirtyHit3.compareTo(minDirty) < 0) {
                return false;
            }
        }
        return true;
    }

    private static BigDecimal dirtyHit3FromJson(String byDifficultyJson) {
        if (byDifficultyJson == null || byDifficultyJson.trim().isEmpty()) {
            return null;
        }
        try {
            JsonNode dirtyHitAt = new ObjectMapper().readTree(byDifficultyJson)
                    .path("dirty").path("hit_at");
            JsonNode node = dirtyHitAt.path("3");
            if (node.isMissingNode() || node.isNull()) {
                node = dirtyHitAt.path(3);
            }
            if (node.isMissingNode() || node.isNull() || !node.isNumber()) {
                return null;
            }
            return node.decimalValue();
        } catch (IOException e) {
            return null;
        }
    }
}
