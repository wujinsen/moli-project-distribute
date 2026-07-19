package com.moli.knowledge.server.guard;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class KbPiiRedactorTest {

    private final KbPiiRedactor redactor = new KbPiiRedactor();

    @Test
    public void redact_email() {
        KbPiiRedactor.PiiRedactResult r = redactor.redact(
                "联系我 test@example.com 谢谢",
                Collections.singletonList("email"));
        Assert.assertTrue(r.getRedactedText().contains("[EMAIL]"));
        Assert.assertFalse(r.getRedactedText().contains("test@example.com"));
        Assert.assertTrue(r.getTypes().contains("email"));
    }

    @Test
    public void redact_phone() {
        KbPiiRedactor.PiiRedactResult r = redactor.redact(
                "手机 13800138000 请回电",
                Collections.singletonList("phone"));
        Assert.assertEquals("手机 [PHONE] 请回电", r.getRedactedText());
    }

    @Test
    public void redact_idCard() {
        KbPiiRedactor.PiiRedactResult r = redactor.redact(
                "身份证 11010119900307987X",
                Collections.singletonList("id_card"));
        Assert.assertTrue(r.getRedactedText().contains("[ID_CARD]"));
    }

    @Test
    public void redact_onlyPiiTooShort() {
        KbPiiRedactor.PiiRedactResult r = redactor.redact(
                "13800138000",
                Arrays.asList("email", "phone", "id_card"));
        Assert.assertTrue(r.isTooShortAfterRedact());
    }

    @Test
    public void redact_preservesTechnicalQuestion() {
        KbPiiRedactor.PiiRedactResult r = redactor.redact(
                "Dubbo 超时怎么排查？",
                Arrays.asList("email", "phone", "id_card"));
        Assert.assertEquals("Dubbo 超时怎么排查？", r.getRedactedText());
        Assert.assertTrue(r.getTypes().isEmpty());
    }
}
