package com.moli.knowledge.server.service.impl;

import com.moli.knowledge.server.config.KbLlmProperties;
import com.moli.knowledge.server.entity.KbPlatformLlmConfig;
import com.moli.knowledge.server.llm.KbLlmConfigSource;
import com.moli.knowledge.server.llm.KbLlmEffectiveConfig;
import com.moli.knowledge.server.mapper.KbPlatformLlmConfigMapper;
import com.moli.knowledge.server.util.KbLlmConfigCipher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

public class KbPlatformLlmConfigServiceImplTest {

    private static final String SECRET = "unit-test-kb-llm-secret";

    @Mock
    private KbPlatformLlmConfigMapper platformLlmConfigMapper;
    @Mock
    private KbLlmProperties yamlLlm;

    @InjectMocks
    private KbPlatformLlmConfigServiceImpl service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(yamlLlm.getConfigSecret()).thenReturn(SECRET);
        when(yamlLlm.isEnabled()).thenReturn(true);
        when(yamlLlm.getProvider()).thenReturn("glm");
        when(yamlLlm.getBaseUrl()).thenReturn("https://example.com/v1");
        when(yamlLlm.getApiKey()).thenReturn("yaml-key");
        when(yamlLlm.getModel()).thenReturn("glm-4-flash");
        when(yamlLlm.getTemperature()).thenReturn(0.3);
        when(yamlLlm.getTimeoutSeconds()).thenReturn(90);
    }

    @Test
    public void resolveEffective_usesDatabaseWhenCipherPresent() {
        String plain = "db-api-key-abcdef12";
        KbPlatformLlmConfig row = new KbPlatformLlmConfig();
        row.setId(KbPlatformLlmConfig.SINGLETON_ID);
        row.setEnabled(1);
        row.setProvider("glm");
        row.setBaseUrl("https://db.example.com/v1");
        row.setModel("glm-4-air");
        row.setTemperature(BigDecimal.valueOf(0.2));
        row.setTimeoutSeconds(60);
        row.setApiKeyCipher(KbLlmConfigCipher.encrypt(plain, SECRET));
        row.setApiKeyMask(KbLlmConfigCipher.maskApiKey(plain));
        row.setExtraModels("[\"glm-4-air\",\"glm-4-flash\"]");

        when(platformLlmConfigMapper.selectById(KbPlatformLlmConfig.SINGLETON_ID)).thenReturn(row);

        KbLlmEffectiveConfig cfg = service.resolveEffective();
        Assert.assertEquals(KbLlmConfigSource.DATABASE, cfg.getSource());
        Assert.assertEquals(plain, cfg.getApiKey());
        Assert.assertEquals("https://db.example.com/v1", cfg.getBaseUrl());
        Assert.assertEquals("glm-4-air", cfg.getModel());
        Assert.assertEquals(2, cfg.getExtraModels().size());
        Assert.assertTrue(cfg.usable());
    }

    @Test
    public void resolveEffective_fallsBackToYamlWhenNoCipher() {
        KbPlatformLlmConfig row = new KbPlatformLlmConfig();
        row.setId(KbPlatformLlmConfig.SINGLETON_ID);
        row.setEnabled(0);
        when(platformLlmConfigMapper.selectById(KbPlatformLlmConfig.SINGLETON_ID)).thenReturn(row);

        KbLlmEffectiveConfig cfg = service.resolveEffective();
        Assert.assertEquals(KbLlmConfigSource.YAML_FALLBACK, cfg.getSource());
        Assert.assertEquals("yaml-key", cfg.getApiKey());
        Assert.assertTrue(cfg.usable());
    }
}
