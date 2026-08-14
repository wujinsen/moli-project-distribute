package com.moli.user.center.common.domain.dto.operation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = OperationDeployServiceKeyValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface OperationDeployServiceKey {

    String message() default "serviceKey 不在 ops.deploy.services 允许列表";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
