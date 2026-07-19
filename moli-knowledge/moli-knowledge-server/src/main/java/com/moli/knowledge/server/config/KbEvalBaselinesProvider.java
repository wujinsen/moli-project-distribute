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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 读取 {@code kb/eval/baselines.json}（§1.1 单一真相）；Java 只读，不写基线。
 */
@Component
public class KbEvalBaselinesProvider {

    private static final Logger log = LoggerFactory.getLogger(KbEvalBaselinesProvider.class);

    private static final List<String> STRATEGY_ORDER = List.of("ngram", "hybrid", "hybrid-rerank");

    @Value("${kb.eval.baselines-path:}")
    private String baselinesPath;

    private JsonNode root;
    private Map<String, BigDecimal> baselineHit3ByStrategy = Map.of();

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
        baselineHit3ByStrategy = Map.copyOf(map);
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
}
