package com.vapor.model.auth;

import java.time.Instant;
import java.util.Set;

/**
 * 登录响应体。
 *
 * @param userId 用户 ID
 * @param roles 角色集合
 * @param token Access Token
 * @param refreshToken Refresh Token（用于无感刷新）
 * @param expiresAt Access Token 过期时间
 * @param refreshExpiresAt Refresh Token 过期时间
 */
public record LoginResponse(
        Long userId,
        Set<String> roles,
        String token,
        String refreshToken,
        Instant expiresAt,
        Instant refreshExpiresAt
) {
}
