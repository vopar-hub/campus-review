package com.vapor.model.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 密码强度校验器。
 *
 * 验证规则：
 * - 最少 6 位字符
 * - 至少包含一个字母和一个数字
 */
public class PasswordValidator implements ConstraintValidator<Password, String> {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z]).{6,}$");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return isValidPassword(password);
    }

    /**
     * 静态工具方法：验证密码是否符合要求。
     *
     * @param password 密码
     * @return 是否有效
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.isBlank()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * 静态工具方法：验证密码并返回错误消息。
     *
     * @param password 密码
     * @return 如果无效返回错误消息，有效返回 null
     */
    public static String validatePassword(String password) {
        if (password == null || password.isBlank()) {
            return "密码不能为空";
        }
        if (password.length() < 6) {
            return "密码长度至少为 6 位";
        }
        if (!password.matches(".*[a-zA-Z].*")) {
            return "密码必须包含字母";
        }
        if (!password.matches(".*[0-9].*")) {
            return "密码必须包含数字";
        }
        return null;
    }
}
