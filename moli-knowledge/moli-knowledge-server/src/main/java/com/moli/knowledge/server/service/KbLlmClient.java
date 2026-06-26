package com.moli.knowledge.server.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbLlmProperties;
import lombok.extern.slf4j.Slf4j;
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
 * OpenAI 兼容 LLM HTTP 客户端（Ask / Wiki AI 改稿共用）。
 */
@Slf4j
@Service
public class KbLlmClient {

    @Resource
    private KbLlmProperties llm;

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
            throw new BaseException("LLM 未配置或已禁用（kb.llm.enabled/api-key）");
        }
    }

    /** 非流式 chat/completions，返回 assistant 文本。 */
    public String chat(String systemPrompt, String userPrompt) {
        assertUsable();
        try {
            return doChat(systemPrompt, userPrompt);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.warn("LLM 调用失败: {}", e.getMessage());
            throw new BaseException("LLM 调用失败：" + e.getMessage());
        }
    }

    private String doChat(String systemPrompt, String userPrompt) throws Exception {
        String url = llm.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";

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
        payload.put("model", llm.getModel());
        payload.put("messages", messages);
        payload.put("temperature", llm.getTemperature());
        payload.put("stream", false);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + llm.getApiKey());
            conn.setDoOutput(true);
            int timeout = (llm.getTimeoutSeconds() == null ? 90 : llm.getTimeoutSeconds()) * 1000;
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
}
