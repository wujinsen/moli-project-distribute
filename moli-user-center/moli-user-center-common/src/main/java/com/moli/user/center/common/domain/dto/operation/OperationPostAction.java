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
@Constraint(validatedBy = OperationPostActionValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface OperationPostAction {

    String message() default "postAction 不支持；允许 none / nginxReload / unzipToDist / restartService:{key} / custom";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
