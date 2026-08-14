package com.moli.user.center.common.domain.dto.operation;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OperationServerRoleValidator implements ConstraintValidator<OperationServerRole, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        return OperationServerRoles.isValid(value.trim());
    }
}
