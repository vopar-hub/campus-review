package com.vapor.model.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求体。
 *
 * @param account 登录账号（邮箱或学号）
 * @param password 密码
 */
public record LoginRequest(
        @NotBlank String account,
        @NotBlank String password
) {
}
