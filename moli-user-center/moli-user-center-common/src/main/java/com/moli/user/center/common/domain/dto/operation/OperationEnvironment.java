package com.moli.user.center.common.domain.dto.operation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 运维环境枚举：1 dev / 2 test / 3 pre / 4 pro；允许 null（不填）。
 */
@Documented
@Constraint(validatedBy = OperationEnvironmentValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface OperationEnvironment {

    String message() default "environment 必须为 1~4（dev/test/pre/pro）";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
