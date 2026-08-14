package com.moli.user.center.common.domain.dto.operation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OperationDeployServiceKeyValidator implements ConstraintValidator<OperationDeployServiceKey, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        String key = OperationDeployConstants.normalizeServiceKey(value);
        return key != null && OperationDeployServiceCatalog.isKnownKey(key);
    }
}
