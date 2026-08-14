package com.moli.user.center.common.domain.dto.operation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OperationDeployTaskActionValidator implements ConstraintValidator<OperationDeployTaskAction, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        String action = OperationDeployConstants.normalizeAction(value);
        return action != null && OperationDeployConstants.TASK_ACTIONS.contains(action);
    }
}
