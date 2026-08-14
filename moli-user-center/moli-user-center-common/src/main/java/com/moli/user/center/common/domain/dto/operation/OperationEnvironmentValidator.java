package com.moli.user.center.common.domain.dto.operation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OperationEnvironmentValidator implements ConstraintValidator<OperationEnvironment, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value >= 1 && value <= 4;
    }
}
