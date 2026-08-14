package com.moli.knowledge.server.llm;

import com.moli.knowledge.server.config.KbLlmRouterProperties;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.moli.common.exception.BaseException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * OpenAI 兼容 LLM HTTP 适配器（从 KbLlmClient 抽出，AI-8 Phase A）。
 */
@Component
public class KbLlmProviderAdapter {

    /**
     * 与 AI-8 前 {@code KbLlmClient.doChat} 一致：HTTP &gt;= 400 一律 {@link BaseException}（无 retryable 分类）。
     */
    public String chatLegacy(KbLlmEffectiveConfig cfg, String systemPrompt, String userPrompt, String modelOverride)
            throws Exception {
        if (cfg == null || !cfg.usable()) {
            throw new BaseException("LLM 未配置或已禁用");
        }
        String url = cfg.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

        JSONArray messages = new JSONArray();
        JSONObject sys = new JSONObject();
        sys.put("role", "system");
        sys.put("content", systemPrompt);
        messages.add(sys);
        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content", userPrompt);
        messages.add(user);

        JSONObject payload = new JSONObject();
        String model = modelOverride != null && !modelOverride.trim().isEmpty()
                ? modelOverride.trim() : cfg.getModel();
        payload.put("model", model);
        payload.put("messages", messages);
        payload.put("temperature", cfg.getTemperature());
        payload.put("stream", false);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + cfg.getApiKey());
            conn.setDoOutput(true);
            int timeout = cfg.getTimeoutSeconds() * 1000;
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(JSON.toJSONString(payload).getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            String body = readStream(code >= 400 ? conn.getErrorStream() : conn.getInputStream());
            if (code >= 400) {
                throw new BaseException("HTTP " + code + ": "
                        + body.substring(0, Math.min(300, body.length())));
            }
            JSONObject json = JSON.parseObject(body);
            return json.getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getString("content");
        } finally {
            conn.disconnect();
        }
    }

    /** failover 路由用：429/5xx/超时/连接失败抛 {@link LlmRetryableException}。 */
    public String chat(KbLlmEffectiveConfig cfg, String systemPrompt, String userPrompt, String modelOverride)
            throws Exception {
        if (cfg == null || !cfg.usable()) {
            throw new BaseException("LLM 未配置或已禁用");
        }
        String url = cfg.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

        JSONArray messages = new JSONArray();
        JSONObject sys = new JSONObject();
        sys.put("role", "system");
        sys.put("content", systemPrompt);
        messages.add(sys);
        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content", userPrompt);
        messages.add(user);

        JSONObject payload = new JSONObject();
        String model = modelOverride != null && !modelOverride.trim().isEmpty()
                ? modelOverride.trim() : cfg.getModel();
        payload.put("model", model);
        payload.put("messages", messages);
        payload.put("temperature", cfg.getTemperature());
        payload.put("stream", false);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + cfg.getApiKey());
            conn.setDoOutput(true);
            int timeout = cfg.getTimeoutSeconds() * 1000;
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(JSON.toJSONString(payload).getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            String body = readStream(code >= 400 ? conn.getErrorStream() : conn.getInputStream());
            if (code == 429 || code >= 500) {
                throw new LlmRetryableException("HTTP " + code + ": "
                        + body.substring(0, Math.min(300, body.length())), code);
            }
            if (code >= 400) {
                throw new BaseException("HTTP " + code + ": "
                        + body.substring(0, Math.min(300, body.length())));
            }
            JSONObject json = JSON.parseObject(body);
            return json.getJSONArray("choices").getJSONObject(0)
                    .getJSONObject("message").getString("content");
        } catch (BaseException e) {
            throw e;
        } catch (LlmRetryableException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            throw new LlmRetryableException("LLM 请求超时", e);
        } catch (java.net.ConnectException | java.net.UnknownHostException e) {
            throw new LlmRetryableException("LLM 连接失败: " + e.getMessage(), e);
        } finally {
            conn.disconnect();
        }
    }

    KbLlmEffectiveConfig resolveFallbackConfig(KbLlmRouterProperties.Fallback entry,
                                               KbLlmEffectiveConfig primary) {
        String apiKey = System.getenv(entry.getApiKeyEnv());
        if (StringUtils.isBlank(apiKey)) {
            return null;
        }
        int timeout = entry.getTimeoutSeconds() != null && entry.getTimeoutSeconds() > 0
                ? entry.getTimeoutSeconds()
                : primary.getTimeoutSeconds();
        return KbLlmEffectiveConfig.builder()
                .enabled(true)
                .provider(entry.getProvider().trim())
                .baseUrl(entry.getBaseUrl().trim())
                .apiKey(apiKey.trim())
                .apiKeyMask(null)
                .model(entry.getModel().trim())
                .temperature(primary.getTemperature())
                .timeoutSeconds(timeout)
                .extraModels(primary.getExtraModels())
                .source(KbLlmConfigSource.YAML_FALLBACK)
                .build();
    }

    private static String readStream(InputStream in) throws Exception {
        if (in == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }
}
