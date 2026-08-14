package com.moli.knowledge.server.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.moli.knowledge.server.config.KbResearchProperties;
import com.moli.knowledge.server.dto.ResearchRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * deep-research sidecar HTTP 客户端（POST /v1/research）。
 */
@Slf4j
@Component
public class KbResearchClient {

    private final KbResearchProperties researchProperties;

    public KbResearchClient(KbResearchProperties researchProperties) {
        this.researchProperties = researchProperties;
    }

    public JSONObject runResearch(String runId, ResearchRequest request, String authToken,
                                  List<Long> resolvedSpaceIds) {
        if (!researchProperties.configured()) {
            throw new KbResearchSidecarException("kb.research.sidecar-base-url not configured");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("runId", runId);
        body.put("topic", request.getTopic());
        if (request.getSpaceId() != null) {
            body.put("spaceId", request.getSpaceId());
        }
        if (request.getSpaceIds() != null && !request.getSpaceIds().isEmpty()) {
            body.put("spaceIds", request.getSpaceIds());
        } else if (resolvedSpaceIds != null && resolvedSpaceIds.size() == 1) {
            body.put("spaceId", resolvedSpaceIds.get(0));
        } else if (resolvedSpaceIds != null && resolvedSpaceIds.size() > 1) {
            body.put("spaceIds", resolvedSpaceIds);
        }
        if (authToken != null && !authToken.trim().isEmpty()) {
            body.put("authToken", authToken.trim());
        }

        Map<String, Object> options = new HashMap<>();
        options.put("maxSections", researchProperties.normalizedMaxSections(request.getMaxSections()));
        options.put("maxRetrieveRounds",
                researchProperties.normalizedMaxRetrieveRounds(request.getMaxRetrieveRounds()));
        options.put("latencyBudgetMs",
                (int) researchProperties.normalizedLatencyBudgetMs(request.getLatencyBudgetMs()));
        options.put("topK", request.getTopK() != null && request.getTopK() > 0 ? request.getTopK() : 8);
        options.put("perSectionTopK", researchProperties.getPerSectionTopK());
        options.put("retrievalStrategy", request.getRetrievalStrategy() != null
                ? request.getRetrievalStrategy()
                : researchProperties.getDefaultRetrievalStrategy());
        options.put("graphExpand", request.getGraphExpand());
        boolean agentic = request.getAgentic() != null ? request.getAgentic() : researchProperties.isRetrieverAgentic();
        options.put("agentic", agentic);
        options.put("coverageThreshold", researchProperties.getCoverageThreshold());
        body.put("options", options);

        try {
            String json = postJson("/v1/research", JSON.toJSONString(body));
            return JSON.parseObject(json);
        } catch (KbResearchSidecarException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("deep-research /v1/research failed: {}", ex.getMessage());
            throw new KbResearchSidecarException("sidecar call failed", ex);
        }
    }

    private String postJson(String path, String bodyJson) throws Exception {
        String base = researchProperties.getSidecarBaseUrl().trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        URL url = new URL(base + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        int timeout = researchProperties.normalizedSidecarTimeoutMs();
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
            throw new KbResearchSidecarException("HTTP " + code + ": " + respBody);
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
}
