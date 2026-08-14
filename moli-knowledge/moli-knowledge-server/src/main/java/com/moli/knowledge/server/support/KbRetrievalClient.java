package com.moli.knowledge.server.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.moli.knowledge.server.config.KbSearchProperties;
import com.moli.knowledge.server.dto.retrieval.RerankCandidateDto;
import com.moli.knowledge.server.dto.retrieval.RerankResponseDto;
import com.moli.knowledge.server.dto.retrieval.VectorSearchHit;
import com.moli.knowledge.server.dto.retrieval.VectorSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * kb-retrieval sidecar HTTP 客户端（/search、/rerank）。失败返回空结果，由调用方降级 ngram。
 */
@Slf4j
@Component
public class KbRetrievalClient {

    private static final long WARN_INTERVAL_MS = 60_000L;

    @Resource
    private KbSearchProperties kbSearchProperties;

    private final AtomicLong lastSearchWarnAt = new AtomicLong(0);
    private final AtomicLong lastRerankWarnAt = new AtomicLong(0);

    @PostConstruct
    void warnIfHybridWithoutSidecar() {
        String strategy = kbSearchProperties.normalizedRetrievalStrategy();
        if (!kbSearchProperties.isNgramStrategy(strategy) && !kbSearchProperties.hybridSidecarConfigured()) {
            log.warn("kb.search.retrieval-strategy={} 但 kb.search.vector.base-url 未配置，将降级 ngram",
                    strategy);
        }
    }

    public List<VectorSearchHit> search(String query, List<Long> spaceIds,
                                        List<String> includeKbTypes, List<String> excludeKbTypes) {
        if (!kbSearchProperties.hybridSidecarConfigured()) {
            return Collections.emptyList();
        }
        JSONObject body = new JSONObject();
        body.put("query", query);
        body.put("spaceIds", spaceIds == null ? new ArrayList<>() : spaceIds);
        body.put("topN", kbSearchProperties.normalizedVectorTopN());
        if ((includeKbTypes != null && !includeKbTypes.isEmpty())
                || (excludeKbTypes != null && !excludeKbTypes.isEmpty())) {
            JSONObject filter = new JSONObject();
            if (includeKbTypes != null && !includeKbTypes.isEmpty()) {
                filter.put("kbType", includeKbTypes);
            }
            if (excludeKbTypes != null && !excludeKbTypes.isEmpty()) {
                filter.put("excludeKbType", excludeKbTypes);
            }
            body.put("filter", filter);
        }
        try {
            String json = postJson("/search", body.toJSONString(), kbSearchProperties.normalizedVectorTimeoutMs());
            VectorSearchResponse resp = JSON.parseObject(json, VectorSearchResponse.class);
            if (resp == null || resp.getResults() == null) {
                return Collections.emptyList();
            }
            return resp.getResults();
        } catch (Exception e) {
            warnThrottled(lastSearchWarnAt, "sidecar /search 失败，降级 ngram: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public RerankResponseDto rerank(String query, List<RerankCandidateDto> candidates, int topM) {
        if (!kbSearchProperties.hybridSidecarConfigured() || candidates == null || candidates.isEmpty()) {
            return null;
        }
        JSONObject body = new JSONObject();
        body.put("query", query);
        body.put("candidates", candidates);
        body.put("topM", topM);
        try {
            String json = postJson("/rerank", body.toJSONString(), kbSearchProperties.normalizedRerankTimeoutMs());
            return JSON.parseObject(json, RerankResponseDto.class);
        } catch (Exception e) {
            warnThrottled(lastRerankWarnAt, "sidecar /rerank 失败，跳过精排: {}", e.getMessage());
            return null;
        }
    }

    private String postJson(String path, String bodyJson, int timeoutMs) throws Exception {
        String base = kbSearchProperties.getVector().getBaseUrl().trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        URL url = new URL(base + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        int timeout = timeoutMs <= 0 ? kbSearchProperties.normalizedVectorTimeoutMs() : timeoutMs;
        conn.setConnectTimeout(timeout);
        conn.setReadTimeout(timeout);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        byte[] bytes = bodyJson.getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(bytes);
        }
        int code = conn.getResponseCode();
        InputStream stream = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        String respBody = readStream(stream);
        if (code < 200 || code >= 300) {
            throw new IllegalStateException("HTTP " + code + ": " + StringUtils.abbreviate(respBody, 200));
        }
        return respBody;
    }

    private static String readStream(InputStream stream) throws Exception {
        if (stream == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private void warnThrottled(AtomicLong lastAt, String template, Object arg) {
        long now = System.currentTimeMillis();
        long prev = lastAt.get();
        if (now - prev >= WARN_INTERVAL_MS && lastAt.compareAndSet(prev, now)) {
            log.warn(template, arg);
        }
    }

    /**
     * AI-8 语义缓存近似命中：调用 sidecar {@code POST /embed-query}；失败返回 null（退化为精确路径）。
     */
    public float[] embedQuery(String text, int timeoutMs) {
        if (!kbSearchProperties.hybridSidecarConfigured() || StringUtils.isBlank(text)) {
            return null;
        }
        JSONObject body = new JSONObject();
        body.put("text", text.trim());
        try {
            String json = postJson("/embed-query", body.toJSONString(), timeoutMs);
            JSONObject resp = JSON.parseObject(json);
            if (resp == null || resp.getJSONArray("embedding") == null) {
                return null;
            }
            JSONArray arr = resp.getJSONArray("embedding");
            float[] out = new float[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                out[i] = arr.getFloatValue(i);
            }
            return out;
        } catch (Exception e) {
            log.debug("sidecar /embed-query 失败，跳过近似缓存: {}", e.getMessage());
            return null;
        }
    }
}
