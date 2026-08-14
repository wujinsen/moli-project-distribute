package com.moli.knowledge.server.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moli.knowledge.server.config.KbSyncProperties;
import com.moli.knowledge.server.dto.SyncTriggerVo;
import com.moli.knowledge.server.service.KbSyncAlertService;
import com.moli.knowledge.server.sync.SyncTriggerSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class KbSyncAlertServiceImpl implements KbSyncAlertService {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private KbSyncProperties syncProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate restTemplate;

    @PostConstruct
    void initRestTemplate() {
        KbSyncProperties.Alert alert = syncProperties.getAlert();
        int timeoutMs = Math.max(1, alert.getTimeoutSeconds()) * 1000;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        restTemplate = new RestTemplate(factory);
    }

    @Override
    public void notifyIfFailed(SyncTriggerSource source, String spaceCode,
                               SyncTriggerVo result, Throwable error) {
        KbSyncProperties.Alert alert = syncProperties.getAlert();
        if (!alert.isEnabled()) {
            return;
        }
        if (alert.isScheduledOnly() && source != SyncTriggerSource.SCHEDULED) {
            return;
        }
        if (!isFailure(result, error)) {
            return;
        }
        if (StringUtils.isBlank(alert.getWebhookUrl())) {
            log.warn("[kb-sync-alert] enabled but kb.sync.alert.webhook-url is empty");
            return;
        }

        try {
            String text = buildAlertText(source, spaceCode, result, error, alert);
            String payload = buildWebhookPayload(alert.getType(), text, source, spaceCode, result, error);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(alert.getWebhookUrl(),
                    new HttpEntity<>(payload, headers), String.class);
            log.info("[kb-sync-alert] sent spaceCode={} source={}", spaceCode, source);
        } catch (Exception e) {
            log.error("[kb-sync-alert] webhook post failed spaceCode={} source={}",
                    spaceCode, source, e);
        }
    }

    static boolean isFailure(SyncTriggerVo result, Throwable error) {
        if (error != null) {
            return true;
        }
        return result != null && !result.isSuccess();
    }

    static String buildAlertText(SyncTriggerSource source, String spaceCode,
                                 SyncTriggerVo result, Throwable error,
                                 KbSyncProperties.Alert alert) {
        StringBuilder sb = new StringBuilder();
        sb.append("[知识库 Sync 失败]\n");
        sb.append("时间: ").append(TS.format(LocalDateTime.now())).append('\n');
        sb.append("空间: ").append(StringUtils.defaultString(spaceCode, "-")).append('\n');
        sb.append("来源: ").append(sourceLabel(source)).append('\n');
        if (result != null) {
            sb.append("exitCode: ").append(result.getExitCode()).append('\n');
        }
        if (error != null) {
            sb.append("异常: ").append(error.getMessage()).append('\n');
        }
        if (result != null && alert.isIncludeOutputTail()
                && StringUtils.isNotBlank(result.getOutputTail())) {
            sb.append("输出摘要:\n");
            sb.append(truncate(result.getOutputTail(), alert.getOutputTailMaxChars()));
        }
        return sb.toString().trim();
    }

    String buildWebhookPayload(String type, String text, SyncTriggerSource source,
                               String spaceCode, SyncTriggerVo result, Throwable error)
            throws Exception {
        String normalized = StringUtils.defaultIfBlank(type, "feishu").toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "wecom":
            case "wework":
            case "wechat":
                return buildWeComPayload(text);
            case "generic":
                return buildGenericPayload(text, source, spaceCode, result, error);
            case "feishu":
            case "lark":
            default:
                return buildFeishuPayload(text);
        }
    }

    static String buildFeishuPayload(String text) throws Exception {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("text", text);
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("msg_type", "text");
        root.put("content", content);
        return new ObjectMapper().writeValueAsString(root);
    }

    static String buildWeComPayload(String text) throws Exception {
        Map<String, Object> textNode = new LinkedHashMap<>();
        textNode.put("content", text);
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("msgtype", "text");
        root.put("text", textNode);
        return new ObjectMapper().writeValueAsString(root);
    }

    String buildGenericPayload(String text, SyncTriggerSource source, String spaceCode,
                               SyncTriggerVo result, Throwable error) throws Exception {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("title", "知识库 Sync 失败");
        root.put("message", text);
        root.put("spaceCode", spaceCode);
        root.put("source", source.name());
        root.put("exitCode", result == null ? null : result.getExitCode());
        root.put("error", error == null ? null : error.getMessage());
        root.put("timestamp", TS.format(LocalDateTime.now()));
        return objectMapper.writeValueAsString(root);
    }

    private static String sourceLabel(SyncTriggerSource source) {
        if (source == null) {
            return "unknown";
        }
        switch (source) {
            case SCHEDULED:
                return "定时任务";
            case MANUAL:
                return "手动触发";
            case AFTER_EDIT:
                return "编辑后自动";
            default:
                return source.name();
        }
    }

    private static String truncate(String text, int maxChars) {
        if (text == null || maxChars <= 0 || text.length() <= maxChars) {
            return text;
        }
        return text.substring(text.length() - maxChars);
    }
}
