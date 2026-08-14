package com.moli.knowledge.server.service.impl;

import com.moli.knowledge.server.config.KbLlmProperties;
import com.moli.knowledge.server.dto.KbPlatformLlmConfigUpdateRequest;
import com.moli.knowledge.server.dto.KbPlatformLlmConfigVo;
import com.moli.knowledge.server.entity.KbPlatformLlmConfig;
import com.moli.knowledge.server.llm.KbLlmConfigSource;
import com.moli.knowledge.server.llm.KbLlmEffectiveConfig;
import com.moli.knowledge.server.llm.KbLlmRuntime;
import com.moli.knowledge.server.mapper.KbPlatformLlmConfigMapper;
import com.moli.knowledge.server.service.KbAclService;
import com.moli.knowledge.server.service.KbLlmClient;
import com.moli.knowledge.server.util.KbLlmConfigCipher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KbPlatformLlmConfigServiceImplSaveTest {

    private static final String SECRET = "unit-test-kb-llm-secret";

    @Mock
    private KbPlatformLlmConfigMapper platformLlmConfigMapper;
    @Mock
    private KbLlmProperties yamlLlm;
    @Mock
    private KbLlmRuntime llmRuntime;
    @Mock
    private KbAclService kbAclService;
    @Mock
    private KbLlmClient kbLlmClient;

    @InjectMocks
    private KbPlatformLlmConfigServiceImpl service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(yamlLlm.resolveConfigSecret()).thenReturn(SECRET);
        when(yamlLlm.configSecretConfigured()).thenReturn(true);
    }

    @Test
    public void save_encryptsApiKeyAndRefreshesRuntime() {
        KbPlatformLlmConfig row = existingRow();
        when(platformLlmConfigMapper.selectById(KbPlatformLlmConfig.SINGLETON_ID)).thenReturn(row);
        doNothing().when(kbAclService).assertPlatformLlmManage();

        KbPlatformLlmConfigUpdateRequest req = new KbPlatformLlmConfigUpdateRequest();
        req.setEnabled(true);
        req.setProvider("glm");
        req.setBaseUrl("https://open.bigmodel.cn/api/paas/v4");
        req.setModel("glm-4-flash");
        req.setTemperature(0.2);
        req.setTimeoutSeconds(60);
        req.setExtraModels(Arrays.asList("glm-4-flash", "glm-4-air"));
        req.setApiKey("new-secret-key-9999");

        KbLlmEffectiveConfig refreshed = KbLlmEffectiveConfig.builder()
                .enabled(true)
                .provider("glm")
                .baseUrl("https://open.bigmodel.cn/api/paas/v4")
                .apiKey("new-secret-key-9999")
                .apiKeyMask("****9999")
                .model("glm-4-flash")
                .temperature(0.2)
                .timeoutSeconds(60)
                .extraModels(Arrays.asList("glm-4-flash", "glm-4-air"))
                .source(KbLlmConfigSource.DATABASE)
                .build();
        when(llmRuntime.current()).thenReturn(refreshed);

        KbPlatformLlmConfigVo vo = service.save(req);
        Assert.assertTrue(vo.getAvailable());
        Assert.assertTrue(vo.getPersistedInDatabase());
        Assert.assertTrue(vo.getEncryptionReady());
        Assert.assertEquals("database", vo.getSource());
        verify(platformLlmConfigMapper).updateById(any(KbPlatformLlmConfig.class));
        verify(llmRuntime).refresh();
    }

    @Test
    public void save_clearApiKey_wipesCipher() {
        KbPlatformLlmConfig row = existingRow();
        row.setApiKeyCipher(KbLlmConfigCipher.encrypt("old-key-1234", SECRET));
        row.setApiKeyMask("****1234");
        when(platformLlmConfigMapper.selectById(KbPlatformLlmConfig.SINGLETON_ID)).thenReturn(row);
        doNothing().when(kbAclService).assertPlatformLlmManage();

        KbPlatformLlmConfigUpdateRequest req = new KbPlatformLlmConfigUpdateRequest();
        req.setEnabled(true);
        req.setProvider("glm");
        req.setBaseUrl("https://open.bigmodel.cn/api/paas/v4");
        req.setModel("glm-4-flash");
        req.setClearApiKey(true);

        when(llmRuntime.current()).thenReturn(KbLlmEffectiveConfig.builder()
                .enabled(true)
                .provider("glm")
                .baseUrl("https://open.bigmodel.cn/api/paas/v4")
                .apiKey("")
                .model("glm-4-flash")
                .temperature(0.3)
                .timeoutSeconds(90)
                .extraModels(Collections.emptyList())
                .source(KbLlmConfigSource.YAML_FALLBACK)
                .build());

        service.save(req);
        Assert.assertNull(row.getApiKeyCipher());
        Assert.assertNull(row.getApiKeyMask());
    }

    private static KbPlatformLlmConfig existingRow() {
        KbPlatformLlmConfig row = new KbPlatformLlmConfig();
        row.setId(KbPlatformLlmConfig.SINGLETON_ID);
        row.setEnabled(0);
        row.setProvider("deepseek");
        row.setBaseUrl("https://api.deepseek.com/v1");
        row.setModel("deepseek-chat");
        row.setTemperature(BigDecimal.valueOf(0.3));
        row.setTimeoutSeconds(90);
        return row;
    }
}
