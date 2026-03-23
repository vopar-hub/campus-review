package com.vapor.model.auth;

import com.vapor.model.validation.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 注册请求体。
 *
 * @param email 邮箱
 * @param studentNo 学号
 * @param password 密码
 * @param nickname 昵称
 */
public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank String studentNo,
        @NotBlank @Password @Size(min = 6, max = 50) String password,
        @NotBlank @Size(min = 1, max = 50) String nickname
) {
}
