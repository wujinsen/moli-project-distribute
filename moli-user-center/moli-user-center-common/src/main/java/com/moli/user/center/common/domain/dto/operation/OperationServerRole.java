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
 * 服务器角色：app / db / cache / mq / gateway / bastion / middleware / other；允许 null。
 */
@Documented
@Constraint(validatedBy = OperationServerRoleValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface OperationServerRole {

    String message() default "serverRole 无效，可选 app/db/cache/mq/gateway/bastion/middleware/other";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
