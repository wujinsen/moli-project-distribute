package com.moli.user.center.server.operation.support;

import com.moli.user.center.server.operation.config.OperationSecretProperties;
import com.moli.user.center.server.operation.util.OperationSecretCipher;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class OperationSecretSupport {

    @Resource
    private OperationSecretProperties secretProperties;

    public String encryptForStorage(String plainPassword) {
        if (StringUtils.isBlank(plainPassword)) {
            return null;
        }
        return OperationSecretCipher.encrypt(plainPassword.trim(), secretProperties.getKey());
    }

    public boolean hasSecret(String stored) {
        return StringUtils.isNotBlank(stored);
    }

    public String mask(String stored) {
        if (StringUtils.isBlank(stored)) {
            return null;
        }
        return OperationSecretCipher.maskSecret(resolvePlain(stored));
    }

    public String resolvePlain(String stored) {
        if (StringUtils.isBlank(stored)) {
            return null;
        }
        if (OperationSecretCipher.looksLikeCipherText(stored)) {
            try {
                return OperationSecretCipher.decrypt(stored, secretProperties.getKey());
            } catch (Exception ignored) {
                // legacy plaintext that happens to look like base64
            }
        }
        return stored.trim();
    }
}
