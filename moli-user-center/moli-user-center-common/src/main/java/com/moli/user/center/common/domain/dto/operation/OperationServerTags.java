package com.moli.user.center.common.domain.dto.operation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = OperationServerTagsValidator.class)
@Target(FIELD)
@Retention(RUNTIME)
public @interface OperationServerTags {

    String message() default "tags 无效：每项 1-32 字符，仅小写字母/数字/:-_，最多 20 个";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
