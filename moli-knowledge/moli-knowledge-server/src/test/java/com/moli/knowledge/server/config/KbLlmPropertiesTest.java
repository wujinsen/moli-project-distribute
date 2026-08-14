package com.moli.knowledge.server.config;

import org.junit.Assert;
import org.junit.Test;

public class KbLlmPropertiesTest {

    @Test
    public void resolveConfigSecret_prefersYaml() {
        KbLlmProperties props = new KbLlmProperties();
        props.setConfigSecret(" yaml-secret ");
        Assert.assertEquals("yaml-secret", props.resolveConfigSecret());
        Assert.assertTrue(props.configSecretConfigured());
    }

    @Test
    public void resolveConfigSecret_fallsBackToEnvWhenYamlBlank() {
        String env = System.getenv("KB_LLM_CONFIG_SECRET");
        org.junit.Assume.assumeTrue(env != null && !env.trim().isEmpty());

        KbLlmProperties props = new KbLlmProperties();
        props.setConfigSecret("");
        Assert.assertEquals(env.trim(), props.resolveConfigSecret());
        Assert.assertTrue(props.configSecretConfigured());
    }
}
