package com.moli.knowledge.server.llm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.moli.knowledge.server.config.KbLlmCacheProperties;
import com.moli.knowledge.server.support.KbRetrievalClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * AI-8 Redis 语义缓存：精确键（默认）+ 可选 embedding 近似命中。
 */
@Slf4j
@Component
public class KbLlmSemanticCache {

    @Resource
    private KbLlmCacheProperties cacheProperties;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private KbRetrievalClient kbRetrievalClient;

    public Optional<KbLlmSemanticCacheEntry> lookup(String scene, String model, String systemPrompt,
                                                    String userPrompt) {
        if (!cacheProperties.isEnabled()) {
            return Optional.empty();
        }
        String exactKey = KbLlmCacheKeyBuilder.buildExactKey(userPrompt, scene, model, systemPrompt);
        KbLlmSemanticCacheEntry exact = readEntry(exactKey);
        if (exact != null) {
            return Optional.of(exact);
        }
        if (!cacheProperties.isApproxEnabled()) {
            return Optional.empty();
        }
        return lookupApprox(scene, model, systemPrompt, userPrompt);
    }

    public void put(String scene, String model, String systemPrompt, String userPrompt, String answer,
                    String provider, String actualModel) {
        if (!cacheProperties.isEnabled() || StringUtils.isBlank(answer)) {
            return;
        }
        String exactKey = KbLlmCacheKeyBuilder.buildExactKey(userPrompt, scene, model, systemPrompt);
        KbLlmSemanticCacheEntry entry = new KbLlmSemanticCacheEntry(
                answer, provider, actualModel, System.currentTimeMillis());
        try {
            stringRedisTemplate.opsForValue().set(exactKey, JSON.toJSONString(entry),
                    cacheProperties.getTtlSeconds(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("[kb-llm-cache] put exact failed: {}", e.getMessage());
            return;
        }
        if (cacheProperties.isApproxEnabled()) {
            indexApproxEntry(scene, model, systemPrompt, userPrompt, exactKey);
        }
    }

    private Optional<KbLlmSemanticCacheEntry> lookupApprox(String scene, String model, String systemPrompt,
                                                           String userPrompt) {
        float[] queryEmb = kbRetrievalClient.embedQuery(userPrompt, cacheProperties.getEmbedTimeoutMs());
        if (queryEmb == null || queryEmb.length == 0) {
            return Optional.empty();
        }
        String ctxFp = KbLlmCacheKeyBuilder.contextFingerprint(systemPrompt);
        String indexKey = KbLlmCacheKeyBuilder.vecIndexKey(scene, model);
        try {
            List<String> raw = stringRedisTemplate.opsForList().range(indexKey, 0, -1);
            if (raw == null || raw.isEmpty()) {
                return Optional.empty();
            }
            double threshold = cacheProperties.getSimilarityThreshold();
            String bestKey = null;
            double bestScore = threshold;
            for (String line : raw) {
                JSONObject obj = JSON.parseObject(line);
                if (obj == null) {
                    continue;
                }
                if (!ctxFp.equals(obj.getString("contextFingerprint"))) {
                    continue;
                }
                JSONArray arr = obj.getJSONArray("embedding");
                if (arr == null || arr.isEmpty()) {
                    continue;
                }
                float[] emb = toFloatArray(arr);
                double score = cosine(queryEmb, emb);
                if (score >= bestScore) {
                    bestScore = score;
                    bestKey = obj.getString("cacheKey");
                }
            }
            if (bestKey == null) {
                return Optional.empty();
            }
            KbLlmSemanticCacheEntry entry = readEntry(bestKey);
            return entry == null ? Optional.empty() : Optional.of(entry);
        } catch (Exception e) {
            log.warn("[kb-llm-cache] approx lookup failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private void indexApproxEntry(String scene, String model, String systemPrompt, String userPrompt,
                                  String exactKey) {
        float[] embedding = kbRetrievalClient.embedQuery(userPrompt, cacheProperties.getEmbedTimeoutMs());
        if (embedding == null || embedding.length == 0) {
            return;
        }
        JSONObject row = new JSONObject();
        row.put("cacheKey", exactKey);
        row.put("contextFingerprint", KbLlmCacheKeyBuilder.contextFingerprint(systemPrompt));
        row.put("embedding", toJsonArray(embedding));
        String indexKey = KbLlmCacheKeyBuilder.vecIndexKey(scene, model);
        try {
            stringRedisTemplate.opsForList().leftPush(indexKey, row.toJSONString());
            trimApproxIndex(indexKey);
        } catch (Exception e) {
            log.warn("[kb-llm-cache] approx index failed: {}", e.getMessage());
        }
    }

    private void trimApproxIndex(String indexKey) {
        int max = cacheProperties.getApproxMaxEntries();
        if (max <= 0) {
            return;
        }
        Long size = stringRedisTemplate.opsForList().size(indexKey);
        if (size != null && size > max) {
            stringRedisTemplate.opsForList().trim(indexKey, 0, max - 1L);
        }
    }

    private KbLlmSemanticCacheEntry readEntry(String key) {
        try {
            String json = stringRedisTemplate.opsForValue().get(key);
            if (StringUtils.isBlank(json)) {
                return null;
            }
            return JSON.parseObject(json, KbLlmSemanticCacheEntry.class);
        } catch (Exception e) {
            log.warn("[kb-llm-cache] read failed key={}: {}", key, e.getMessage());
            return null;
        }
    }

    static double cosine(float[] a, float[] b) {
        int n = Math.min(a.length, b.length);
        if (n == 0) {
            return 0;
        }
        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < n; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) {
            return 0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private static float[] toFloatArray(JSONArray arr) {
        float[] out = new float[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            out[i] = arr.getFloatValue(i);
        }
        return out;
    }

    private static JSONArray toJsonArray(float[] values) {
        JSONArray arr = new JSONArray();
        for (float v : values) {
            arr.add(v);
        }
        return arr;
    }
}
