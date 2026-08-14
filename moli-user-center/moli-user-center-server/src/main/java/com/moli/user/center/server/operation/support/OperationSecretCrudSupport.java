package com.moli.user.center.server.operation.support;

import com.moli.user.center.common.domain.vo.OperationSecretRevealVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 台账凭据字段：加密入库、更新合并、列表脱敏（Phase R4）。
 */
@Component
public class OperationSecretCrudSupport {

    @Resource
    private OperationSecretSupport secretSupport;

    public String encryptOnSave(String plainPassword) {
        return secretSupport.encryptForStorage(plainPassword);
    }

    public String mergeOnUpdate(String requestPlain, String existingStored) {
        if (StringUtils.isNotBlank(requestPlain)) {
            return secretSupport.encryptForStorage(requestPlain);
        }
        return existingStored;
    }

    public String mergeEncryptedOnUpdate(String requestPlain, String existingStored) {
        return mergeOnUpdate(requestPlain, existingStored);
    }

    public OperationSecretRevealVo reveal(String stored) {
        return new OperationSecretRevealVo(secretSupport.resolvePlain(stored));
    }

    public boolean passwordConfigured(String stored) {
        return secretSupport.hasSecret(stored);
    }

    public String passwordMask(String stored) {
        return secretSupport.mask(stored);
    }
}
