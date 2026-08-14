package com.moli.knowledge.server.util;

import com.moli.common.exception.BaseException;
import org.junit.Assert;
import org.junit.Test;

public class KbLlmUrlValidatorTest {

    @Test
    public void acceptsPublicHttps() {
        KbLlmUrlValidator.validateBaseUrl("https://api.deepseek.com/v1");
    }

    @Test(expected = BaseException.class)
    public void rejectsLocalhost() {
        KbLlmUrlValidator.validateBaseUrl("http://127.0.0.1/v1");
    }
}
