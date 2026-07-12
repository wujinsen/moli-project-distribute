package com.moli.user.center.common.domain.dto.operation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class OperationServerTagsValidator implements ConstraintValidator<OperationServerTags, List<String>> {

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        return OperationServerTagsSupport.isValidList(value);
    }
}
