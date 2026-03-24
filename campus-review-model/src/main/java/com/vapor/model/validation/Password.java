package com.vapor.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 密码强度校验注解。
 *
 * 密码要求：
 * - 最少 6 位字符
 * - 至少包含一个字母和一个数字
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface Password {

    String message() default "密码格式不正确，需至少 6 位且包含字母和数字";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
