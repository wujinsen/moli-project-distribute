package com.moli.knowledge.server.llm;

import com.moli.knowledge.server.service.KbPlatformLlmConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 线程安全的 LLM 生效配置快照（T19a）。
 * <p>
 * 读取优先级：MySQL {@code kb_platform_llm_config} → {@code kb.llm.*} yaml 兜底。
 */
@Slf4j
@Component
public class KbLlmRuntime {

    private static final KbLlmEffectiveConfig DISABLED = KbLlmEffectiveConfig.builder()
            .enabled(false)
            .provider("deepseek")
            .baseUrl("https://api.deepseek.com/v1")
            .apiKey("")
            .apiKeyMask(null)
            .model("deepseek-chat")
            .temperature(0.3d)
            .timeoutSeconds(90)
            .extraModels(Collections.emptyList())
            .source(KbLlmConfigSource.YAML_FALLBACK)
            .build();

    private final AtomicReference<KbLlmEffectiveConfig> snapshot = new AtomicReference<>(DISABLED);

    @Resource
    private KbPlatformLlmConfigService platformLlmConfigService;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        platformLlmConfigService.ensureSingletonRow();
        refresh();
    }

    public void refresh() {
        try {
            KbLlmEffectiveConfig cfg = platformLlmConfigService.resolveEffective();
            snapshot.set(cfg != null ? cfg : DISABLED);
            log.info("[kb-llm] runtime refreshed source={} usable={}",
                    snapshot.get().getSource(), snapshot.get().usable());
        } catch (Exception e) {
            log.warn("[kb-llm] runtime refresh failed, keep previous snapshot: {}", e.getMessage());
        }
    }

    public KbLlmEffectiveConfig current() {
        return snapshot.get();
    }

    public KbLlmConfigSource getSource() {
        return current().getSource();
    }

    public boolean usable() {
        return current().usable();
    }

    public boolean isEnabled() {
        return current().isEnabled();
    }

    public String getProvider() {
        return current().getProvider();
    }

    public String getBaseUrl() {
        return current().getBaseUrl();
    }

    public String getApiKey() {
        return current().getApiKey();
    }

    public String getApiKeyMask() {
        return current().getApiKeyMask();
    }

    public boolean apiKeyConfigured() {
        return current().apiKeyConfigured();
    }

    public String getModel() {
        return current().getModel();
    }

    public Double getTemperature() {
        return current().getTemperature();
    }

    public Integer getTimeoutSeconds() {
        return current().getTimeoutSeconds();
    }

    public List<String> getExtraModels() {
        return current().getExtraModels();
    }
}
