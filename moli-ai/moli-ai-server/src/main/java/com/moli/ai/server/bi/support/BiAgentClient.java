package com.moli.ai.server.bi.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moli.ai.server.bi.config.BiAgentProperties;
import com.moli.ai.server.bi.dto.agent.BiAgentExplainRequest;
import com.moli.ai.server.bi.dto.agent.BiAgentExplainResponse;
import com.moli.ai.server.bi.dto.agent.BiAgentGenerateRequest;
import com.moli.ai.server.bi.dto.agent.BiAgentGenerateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * ai-agent sidecar HTTP 客户端（§1.2 /agent/generate · /agent/explain）。
 */
@Slf4j
@Component
public class BiAgentClient {

    private final BiAgentProperties agentProperties;
    private final ObjectMapper objectMapper;

    public BiAgentClient(BiAgentProperties agentProperties, ObjectMapper objectMapper) {
        this.agentProperties = agentProperties;
        this.objectMapper = objectMapper;
    }

    public BiAgentGenerateResponse generate(BiAgentGenerateRequest request) {
        return post("/agent/generate", request, BiAgentGenerateResponse.class);
    }

    public BiAgentExplainResponse explain(BiAgentExplainRequest request) {
        return post("/agent/explain", request, BiAgentExplainResponse.class);
    }

    private <T> T post(String path, Object body, Class<T> responseType) {
        if (!agentProperties.configured()) {
            throw new BiAgentUnavailableException("bi.agent.base-url not configured");
        }
        try {
            String json = postJson(path, objectMapper.writeValueAsString(body));
            return objectMapper.readValue(json, responseType);
        } catch (BiAgentUnavailableException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("ai-agent {} failed: {}", path, ex.getMessage());
            throw new BiAgentUnavailableException("sidecar call failed", ex);
        }
    }

    private String postJson(String path, String bodyJson) throws Exception {
        String base = agentProperties.getBaseUrl().trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        URL url = new URL(base + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        int timeout = agentProperties.normalizedTimeoutMs();
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
            throw new BiAgentUnavailableException("HTTP " + code);
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
