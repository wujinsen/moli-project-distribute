package com.moli.user.center.common.domain.dto.operation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OperationPostActionValidator implements ConstraintValidator<OperationPostAction, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        return OperationDeployConstants.isValidPostAction(value);
    }
}
