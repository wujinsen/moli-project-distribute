package com.moli.user.center.server.operation.support;

import com.moli.user.center.common.domain.vo.OperationSecretRevealVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OperationSecretCrudSupportTest {

    @InjectMocks
    private OperationSecretCrudSupport secretCrudSupport;

    @Mock
    private OperationSecretSupport secretSupport;

    @Test
    public void encryptOnSave_delegates_to_secret_support() {
        when(secretSupport.encryptForStorage("plain")).thenReturn("cipher");

        assertEquals("cipher", secretCrudSupport.encryptOnSave("plain"));
        verify(secretSupport).encryptForStorage("plain");
    }

    @Test
    public void mergeOnUpdate_encrypts_when_request_has_plain() {
        when(secretSupport.encryptForStorage("new-pass")).thenReturn("new-cipher");

        assertEquals("new-cipher", secretCrudSupport.mergeOnUpdate("new-pass", "old-cipher"));
    }

    @Test
    public void mergeOnUpdate_keeps_existing_when_request_blank() {
        assertEquals("old-cipher", secretCrudSupport.mergeOnUpdate("", "old-cipher"));
        assertEquals("old-cipher", secretCrudSupport.mergeOnUpdate(null, "old-cipher"));
    }

    @Test
    public void reveal_wraps_plain_from_support() {
        when(secretSupport.resolvePlain("stored")).thenReturn("plain");

        OperationSecretRevealVo vo = secretCrudSupport.reveal("stored");
        assertEquals("plain", vo.getPassword());
    }

    @Test
    public void passwordConfigured_and_mask_delegate() {
        when(secretSupport.hasSecret("stored")).thenReturn(true);
        when(secretSupport.mask("stored")).thenReturn("****word");

        assertTrue(secretCrudSupport.passwordConfigured("stored"));
        assertEquals("****word", secretCrudSupport.passwordMask("stored"));
    }

    @Test
    public void passwordConfigured_false_when_empty() {
        when(secretSupport.hasSecret("")).thenReturn(false);
        assertFalse(secretCrudSupport.passwordConfigured(""));
    }
}
