package com.moli.knowledge.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.moli.common.exception.BaseException;
import com.moli.knowledge.server.config.KbLlmProperties;
import com.moli.knowledge.server.dto.KbPlatformLlmConfigTestRequest;
import com.moli.knowledge.server.dto.KbPlatformLlmConfigTestResultVo;
import com.moli.knowledge.server.dto.KbPlatformLlmConfigUpdateRequest;
import com.moli.knowledge.server.dto.KbPlatformLlmConfigVo;
import com.moli.knowledge.server.entity.KbPlatformLlmConfig;
import com.moli.knowledge.server.llm.KbLlmConfigSource;
import com.moli.knowledge.server.llm.KbLlmEffectiveConfig;
import com.moli.knowledge.server.llm.KbLlmRuntime;
import com.moli.knowledge.server.mapper.KbPlatformLlmConfigMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbLlmClient;
import com.moli.knowledge.server.service.KbPlatformLlmConfigService;
import com.moli.knowledge.server.util.KbLlmConfigCipher;
import com.moli.knowledge.server.util.KbLlmUrlValidator;
import com.moli.knowledge.server.util.ShiroUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class KbPlatformLlmConfigServiceImpl implements KbPlatformLlmConfigService {

    private static final double MIN_TEMPERATURE = 0d;
    private static final double MAX_TEMPERATURE = 2d;
    private static final int MIN_TIMEOUT = 5;
    private static final int MAX_TIMEOUT = 300;

    @Resource
    private KbPlatformLlmConfigMapper platformLlmConfigMapper;
    @Resource
    private KbLlmProperties yamlLlm;
    @Lazy
    @Resource
    private KbLlmRuntime llmRuntime;
    @Resource
    private KbAclService kbAclService;
    @Resource
    private KbLlmClient kbLlmClient;

    @Override
    public void ensureSingletonRow() {
        try {
            if (platformLlmConfigMapper.selectById(KbPlatformLlmConfig.SINGLETON_ID) != null) {
                return;
            }
            KbPlatformLlmConfig row = newRowFromYaml(false);
            row.setId(KbPlatformLlmConfig.SINGLETON_ID);
            Date now = new Date();
            row.setCreateTime(now);
            row.setUpdateTime(now);
            platformLlmConfigMapper.insert(row);
            log.info("[kb-llm] inserted platform llm config placeholder row id=1");
        } catch (DataAccessException e) {
            log.warn("[kb-llm] ensureSingletonRow skipped (table missing?): {}", e.getMessage());
        }
    }

    @Override
    public KbPlatformLlmConfig getSingletonRow() {
        try {
            return platformLlmConfigMapper.selectById(KbPlatformLlmConfig.SINGLETON_ID);
        } catch (DataAccessException e) {
            log.warn("[kb-llm] getSingletonRow failed: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public KbLlmEffectiveConfig resolveEffective() {
        KbPlatformLlmConfig row = getSingletonRow();
        if (row != null && StringUtils.isNotBlank(row.getApiKeyCipher())) {
            try {
                String plainKey = KbLlmConfigCipher.decrypt(row.getApiKeyCipher(), yamlLlm.resolveConfigSecret());
                if (StringUtils.isNotBlank(plainKey)) {
                    return fromDatabaseRow(row, plainKey.trim());
                }
            } catch (Exception e) {
                log.warn("[kb-llm] decrypt platform api-key failed, fallback yaml: {}", e.getMessage());
            }
        }
        if (row != null && intTrue(row.getEnabled()) && StringUtils.isBlank(row.getApiKeyCipher())) {
            // DB 行存在但未存 key：非敏感字段仍走 DB，key 从 yaml 补
            KbLlmEffectiveConfig yaml = fromYamlFallback();
            return KbLlmEffectiveConfig.builder()
                    .enabled(intTrue(row.getEnabled()))
                    .provider(defaultString(row.getProvider(), yaml.getProvider()))
                    .baseUrl(defaultString(row.getBaseUrl(), yaml.getBaseUrl()))
                    .apiKey(yaml.getApiKey())
                    .apiKeyMask(yaml.getApiKeyMask())
                    .model(defaultString(row.getModel(), yaml.getModel()))
                    .temperature(toDouble(row.getTemperature(), yaml.getTemperature()))
                    .timeoutSeconds(toInt(row.getTimeoutSeconds(), yaml.getTimeoutSeconds()))
                    .extraModels(parseExtraModels(row.getExtraModels()))
                    .source(StringUtils.isNotBlank(yaml.getApiKey())
                            ? KbLlmConfigSource.YAML_FALLBACK : KbLlmConfigSource.DATABASE)
                    .build();
        }
        return fromYamlFallback();
    }

    @Override
    public KbPlatformLlmConfigVo getAdminView() {
        kbAclService.assertPlatformLlmManage();
        ensureSingletonRow();
        KbPlatformLlmConfig row = getSingletonRow();
        KbLlmEffectiveConfig effective = llmRuntime.current();
        return toVo(row, effective);
    }

    @Override
    public KbPlatformLlmConfigVo save(KbPlatformLlmConfigUpdateRequest request) {
        kbAclService.assertPlatformLlmManage();
        if (request == null) {
            throw new BaseException("请求体不能为空");
        }
        ensureSingletonRow();
        KbPlatformLlmConfig row = getSingletonRow();
        if (row == null) {
            throw new BaseException("平台 LLM 配置表不可用，请先执行 11_kb_platform_llm_config.sql");
        }

        KbLlmUrlValidator.validateBaseUrl(request.getBaseUrl());
        double temperature = normalizeTemperature(request.getTemperature());
        int timeoutSeconds = normalizeTimeout(request.getTimeoutSeconds());

        row.setEnabled(Boolean.TRUE.equals(request.getEnabled()) ? 1 : 0);
        row.setCallLogEnabled(Boolean.TRUE.equals(request.getCallLogEnabled()) ? 1 : 0);
        row.setProvider(request.getProvider().trim());
        row.setBaseUrl(request.getBaseUrl().trim());
        row.setModel(request.getModel().trim());
        row.setTemperature(BigDecimal.valueOf(temperature));
        row.setTimeoutSeconds(timeoutSeconds);
        row.setExtraModels(serializeExtraModels(request.getExtraModels()));

        if (Boolean.TRUE.equals(request.getClearApiKey())) {
            row.setApiKeyCipher(null);
            row.setApiKeyMask(null);
        } else if (StringUtils.isNotBlank(request.getApiKey())) {
            assertConfigSecretPresent();
            String plain = request.getApiKey().trim();
            row.setApiKeyCipher(KbLlmConfigCipher.encrypt(plain, yamlLlm.resolveConfigSecret()));
            row.setApiKeyMask(KbLlmConfigCipher.maskApiKey(plain));
        }

        row.setUpdateId(ShiroUtils.getUserId());
        row.setUpdateTime(new Date());
        platformLlmConfigMapper.updateById(row);
        llmRuntime.refresh();
        log.info("[kb-llm] platform config saved by user={}", ShiroUtils.getUserId());
        return toVo(row, llmRuntime.current());
    }

    @Override
    public KbPlatformLlmConfigTestResultVo testConnection(KbPlatformLlmConfigTestRequest request) {
        kbAclService.assertPlatformLlmManage();
        KbPlatformLlmConfigTestRequest req = request != null ? request : new KbPlatformLlmConfigTestRequest();
        KbLlmEffectiveConfig cfg = buildConfigForTest(req);
        KbPlatformLlmConfigTestResultVo vo = new KbPlatformLlmConfigTestResultVo();
        vo.setModel(cfg.getModel());
        if (!cfg.usable()) {
            vo.setSuccess(false);
            vo.setError("LLM 未启用或未配置 api-key");
            return vo;
        }
        long start = System.currentTimeMillis();
        try {
            String reply = kbLlmClient.testPing(cfg, req.getMessage());
            vo.setSuccess(true);
            vo.setLatencyMs(System.currentTimeMillis() - start);
            vo.setReplyPreview(truncate(reply, 200));
        } catch (Exception e) {
            vo.setSuccess(false);
            vo.setLatencyMs(System.currentTimeMillis() - start);
            vo.setError(e.getMessage());
        }
        return vo;
    }

    private KbLlmEffectiveConfig buildConfigForTest(KbPlatformLlmConfigTestRequest req) {
        KbLlmEffectiveConfig base = llmRuntime.current();
        KbPlatformLlmConfig row = getSingletonRow();

        boolean enabled = req.getEnabled() != null ? req.getEnabled() : base.isEnabled();
        String provider = StringUtils.defaultIfBlank(req.getProvider(),
                row != null ? row.getProvider() : base.getProvider());
        String baseUrl = StringUtils.defaultIfBlank(req.getBaseUrl(),
                row != null ? row.getBaseUrl() : base.getBaseUrl());
        String model = StringUtils.defaultIfBlank(req.getModel(),
                row != null ? row.getModel() : base.getModel());
        double temperature = req.getTemperature() != null
                ? normalizeTemperature(req.getTemperature())
                : base.getTemperature();
        int timeout = req.getTimeoutSeconds() != null
                ? normalizeTimeout(req.getTimeoutSeconds())
                : base.getTimeoutSeconds();

        String apiKey = base.getApiKey();
        if (StringUtils.isNotBlank(req.getApiKey())) {
            apiKey = req.getApiKey().trim();
        }

        if (StringUtils.isNotBlank(baseUrl)) {
            KbLlmUrlValidator.validateBaseUrl(baseUrl);
        }

        return KbLlmEffectiveConfig.builder()
                .enabled(enabled)
                .provider(provider)
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .apiKeyMask(KbLlmConfigCipher.maskApiKey(apiKey))
                .model(model)
                .temperature(temperature)
                .timeoutSeconds(timeout)
                .extraModels(req.getExtraModels() != null
                        ? normalizeExtraModels(req.getExtraModels())
                        : base.getExtraModels())
                .source(KbLlmConfigSource.DATABASE)
                .build();
    }

    private KbPlatformLlmConfigVo toVo(KbPlatformLlmConfig row, KbLlmEffectiveConfig effective) {
        KbPlatformLlmConfigVo vo = new KbPlatformLlmConfigVo();
        if (row != null) {
            vo.setEnabled(intTrue(row.getEnabled()));
            vo.setCallLogEnabled(resolveCallLogEnabled(row));
            vo.setProvider(row.getProvider());
            vo.setBaseUrl(row.getBaseUrl());
            vo.setModel(row.getModel());
            vo.setTemperature(toDouble(row.getTemperature(), effective.getTemperature()));
            vo.setTimeoutSeconds(toInt(row.getTimeoutSeconds(), effective.getTimeoutSeconds()));
            vo.setExtraModels(parseExtraModels(row.getExtraModels()));
            vo.setApiKeyConfigured(StringUtils.isNotBlank(row.getApiKeyCipher())
                    || (effective.apiKeyConfigured() && effective.getSource() == KbLlmConfigSource.YAML_FALLBACK));
            vo.setApiKeyMask(StringUtils.isNotBlank(row.getApiKeyMask())
                    ? row.getApiKeyMask() : effective.getApiKeyMask());
            vo.setPersistedInDatabase(StringUtils.isNotBlank(row.getApiKeyCipher()));
            if (row.getUpdateTime() != null) {
                vo.setUpdateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(row.getUpdateTime()));
            }
        } else {
            vo.setEnabled(effective.isEnabled());
            vo.setCallLogEnabled(yamlLlm.isCallLogEnabled());
            vo.setProvider(effective.getProvider());
            vo.setBaseUrl(effective.getBaseUrl());
            vo.setModel(effective.getModel());
            vo.setTemperature(effective.getTemperature());
            vo.setTimeoutSeconds(effective.getTimeoutSeconds());
            vo.setExtraModels(effective.getExtraModels());
            vo.setApiKeyConfigured(effective.apiKeyConfigured());
            vo.setApiKeyMask(effective.getApiKeyMask());
            vo.setPersistedInDatabase(false);
        }
        vo.setAvailable(effective.usable());
        vo.setSource(effective.getSource().name().toLowerCase());
        vo.setEncryptionReady(yamlLlm.configSecretConfigured());
        return vo;
    }

    @Override
    public boolean isCallLogEnabled() {
        KbPlatformLlmConfig row = getSingletonRow();
        if (row != null && row.getCallLogEnabled() != null) {
            return intTrue(row.getCallLogEnabled());
        }
        return yamlLlm.isCallLogEnabled();
    }

    private void assertConfigSecretPresent() {
        if (!yamlLlm.configSecretConfigured()) {
            throw new BaseException("未配置 kb.llm.config-secret（KB_LLM_CONFIG_SECRET），无法将 api-key 加密存入数据库");
        }
    }

    private static double normalizeTemperature(Double value) {
        double t = value != null ? value : 0.3d;
        if (t < MIN_TEMPERATURE || t > MAX_TEMPERATURE) {
            throw new BaseException("temperature 须在 " + MIN_TEMPERATURE + "～" + MAX_TEMPERATURE + " 之间");
        }
        return t;
    }

    private static int normalizeTimeout(Integer value) {
        int t = value != null ? value : 90;
        if (t < MIN_TIMEOUT || t > MAX_TIMEOUT) {
            throw new BaseException("timeoutSeconds 须在 " + MIN_TIMEOUT + "～" + MAX_TIMEOUT + " 之间");
        }
        return t;
    }

    private static String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        String s = text.trim();
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    private KbLlmEffectiveConfig fromDatabaseRow(KbPlatformLlmConfig row, String plainKey) {
        return KbLlmEffectiveConfig.builder()
                .enabled(intTrue(row.getEnabled()))
                .provider(defaultString(row.getProvider(), yamlLlm.getProvider()))
                .baseUrl(defaultString(row.getBaseUrl(), yamlLlm.getBaseUrl()))
                .apiKey(plainKey)
                .apiKeyMask(StringUtils.defaultIfBlank(row.getApiKeyMask(), KbLlmConfigCipher.maskApiKey(plainKey)))
                .model(defaultString(row.getModel(), yamlLlm.getModel()))
                .temperature(toDouble(row.getTemperature(), yamlLlm.getTemperature()))
                .timeoutSeconds(toInt(row.getTimeoutSeconds(), yamlLlm.getTimeoutSeconds()))
                .extraModels(parseExtraModels(row.getExtraModels()))
                .source(KbLlmConfigSource.DATABASE)
                .build();
    }

    private KbLlmEffectiveConfig fromYamlFallback() {
        String key = StringUtils.trimToEmpty(yamlLlm.getApiKey());
        return KbLlmEffectiveConfig.builder()
                .enabled(yamlLlm.isEnabled())
                .provider(yamlLlm.getProvider())
                .baseUrl(yamlLlm.getBaseUrl())
                .apiKey(key)
                .apiKeyMask(KbLlmConfigCipher.maskApiKey(key))
                .model(yamlLlm.getModel())
                .temperature(toDouble(null, yamlLlm.getTemperature()))
                .timeoutSeconds(toInt(null, yamlLlm.getTimeoutSeconds()))
                .extraModels(Collections.emptyList())
                .source(KbLlmConfigSource.YAML_FALLBACK)
                .build();
    }

    private KbPlatformLlmConfig newRowFromYaml(boolean copyYamlApiKey) {
        KbPlatformLlmConfig row = new KbPlatformLlmConfig();
        row.setEnabled(yamlLlm.isEnabled() ? 1 : 0);
        row.setCallLogEnabled(yamlLlm.isCallLogEnabled() ? 1 : 0);
        row.setProvider(StringUtils.defaultIfBlank(yamlLlm.getProvider(), "deepseek"));
        row.setBaseUrl(StringUtils.defaultIfBlank(yamlLlm.getBaseUrl(), "https://api.deepseek.com/v1"));
        row.setModel(StringUtils.defaultIfBlank(yamlLlm.getModel(), "deepseek-chat"));
        row.setTemperature(BigDecimal.valueOf(toDouble(null, yamlLlm.getTemperature())));
        row.setTimeoutSeconds(toInt(null, yamlLlm.getTimeoutSeconds()));
        if (copyYamlApiKey && StringUtils.isNotBlank(yamlLlm.getApiKey())
                && yamlLlm.configSecretConfigured()) {
            String plain = yamlLlm.getApiKey().trim();
            row.setApiKeyCipher(KbLlmConfigCipher.encrypt(plain, yamlLlm.resolveConfigSecret()));
            row.setApiKeyMask(KbLlmConfigCipher.maskApiKey(plain));
        }
        return row;
    }

    private static String serializeExtraModels(List<String> models) {
        List<String> normalized = normalizeExtraModels(models);
        if (normalized.isEmpty()) {
            return null;
        }
        return JSON.toJSONString(normalized);
    }

    private static List<String> normalizeExtraModels(List<String> models) {
        if (models == null || models.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> out = new ArrayList<>();
        for (String m : models) {
            if (StringUtils.isNotBlank(m)) {
                out.add(m.trim());
            }
        }
        return out;
    }

    private static boolean intTrue(Integer v) {
        return v != null && v == 1;
    }

    private boolean resolveCallLogEnabled(KbPlatformLlmConfig row) {
        if (row != null && row.getCallLogEnabled() != null) {
            return intTrue(row.getCallLogEnabled());
        }
        return yamlLlm.isCallLogEnabled();
    }

    private static String defaultString(String primary, String fallback) {
        return StringUtils.defaultIfBlank(primary, fallback);
    }

    private static double toDouble(BigDecimal primary, Double fallback) {
        if (primary != null) {
            return primary.doubleValue();
        }
        return fallback != null ? fallback : 0.3d;
    }

    private static int toInt(Integer primary, Integer fallback) {
        if (primary != null) {
            return primary;
        }
        return fallback != null ? fallback : 90;
    }

    private static List<String> parseExtraModels(String json) {
        if (StringUtils.isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            List<String> list = JSON.parseArray(json.trim(), String.class);
            if (list == null || list.isEmpty()) {
                return Collections.emptyList();
            }
            return normalizeExtraModels(list);
        } catch (Exception e) {
            log.warn("[kb-llm] parse extra_models failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
