package com.moli.knowledge.server.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.llm.KbLlmEffectiveConfig;
import com.moli.knowledge.server.llm.KbLlmRuntime;
import com.moli.knowledge.server.support.KbLlmCallScenes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * OpenAI 兼容 LLM HTTP 客户端（Ask / Wiki AI 改稿 / Ingest 共用）。
 * <p>
 * 配置来源：{@link KbLlmRuntime}（DB 优先，yaml 兜底）。
 */
@Slf4j
@Service
public class KbLlmClient {

    private static final String DEFAULT_TEST_SYSTEM = "You are a helpful assistant. Reply briefly.";

    @Resource
    private KbLlmRuntime llm;
    @Resource
    private KbLlmCallLogService callLogService;

    public boolean usable() {
        return llm.usable();
    }

    public String getProvider() {
        return llm.getProvider();
    }

    public String getModel() {
        return llm.getModel();
    }

    public void assertUsable() {
        if (!usable()) {
            throw new BaseException("LLM 未配置或已禁用（平台 LLM 设置或 kb.llm.enabled/api-key）");
        }
    }

    public String chat(String systemPrompt, String userPrompt) {
        return chat(null, null, systemPrompt, userPrompt, null);
    }

    public String chat(String systemPrompt, String userPrompt, String modelOverride) {
        return chat(null, null, systemPrompt, userPrompt, modelOverride);
    }

    public String chat(String scene, Long spaceId, String systemPrompt, String userPrompt) {
        return chat(scene, spaceId, systemPrompt, userPrompt, null);
    }

    /** 带 scene/space 的调用（写入 kb_llm_call_log）。 */
    public String chat(String scene, Long spaceId, String systemPrompt, String userPrompt, String modelOverride) {
        assertUsable();
        long start = System.currentTimeMillis();
        String provider = getProvider();
        String model = resolveModel(modelOverride);
        try {
            String result = doChat(llm.current(), systemPrompt, userPrompt, modelOverride);
            callLogService.recordSuccess(scene, spaceId, provider, model, elapsed(start));
            return result;
        } catch (BaseException e) {
            callLogService.recordFail(scene, spaceId, provider, model, elapsed(start), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.warn("LLM 调用失败: {}", e.getMessage());
            callLogService.recordFail(scene, spaceId, provider, model, elapsed(start), e.getMessage());
            throw new BaseException("LLM 调用失败：" + e.getMessage());
        }
    }

    /** 使用指定配置调用（连通性测试等），不依赖 Runtime 快照。 */
    public String chatWithConfig(KbLlmEffectiveConfig cfg, String systemPrompt, String userPrompt, String modelOverride)
            throws Exception {
        return chatWithConfig(cfg, KbLlmCallScenes.LLM_TEST, null, systemPrompt, userPrompt, modelOverride);
    }

    public String chatWithConfig(KbLlmEffectiveConfig cfg, String scene, Long spaceId,
                                 String systemPrompt, String userPrompt, String modelOverride) throws Exception {
        if (cfg == null || !cfg.usable()) {
            throw new BaseException("LLM 未配置或已禁用");
        }
        long start = System.currentTimeMillis();
        String provider = cfg.getProvider();
        String model = resolveModel(modelOverride, cfg.getModel());
        try {
            String result = doChat(cfg, systemPrompt, userPrompt, modelOverride);
            callLogService.recordSuccess(scene, spaceId, provider, model, elapsed(start));
            return result;
        } catch (BaseException e) {
            callLogService.recordFail(scene, spaceId, provider, model, elapsed(start), e.getMessage());
            throw e;
        } catch (Exception e) {
            callLogService.recordFail(scene, spaceId, provider, model, elapsed(start), e.getMessage());
            throw e;
        }
    }

    public String testPing(KbLlmEffectiveConfig cfg, String userMessage) throws Exception {
        String msg = userMessage == null || userMessage.trim().isEmpty() ? "ping" : userMessage.trim();
        return chatWithConfig(cfg, KbLlmCallScenes.LLM_TEST, null, DEFAULT_TEST_SYSTEM, msg, null);
    }

    private String doChat(KbLlmEffectiveConfig cfg, String systemPrompt, String userPrompt, String modelOverride)
            throws Exception {
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

    private String readStream(InputStream in) throws Exception {
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

    private static long elapsed(long start) {
        return System.currentTimeMillis() - start;
    }

    private String resolveModel(String modelOverride) {
        return resolveModel(modelOverride, getModel());
    }

    private static String resolveModel(String modelOverride, String defaultModel) {
        return StringUtils.isNotBlank(modelOverride) ? modelOverride.trim() : defaultModel;
    }
}
