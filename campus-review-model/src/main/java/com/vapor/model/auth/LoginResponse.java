package com.vapor.model.auth;

import java.time.Instant;
import java.util.Set;

/**
 * 登录响应体。
 *
 * @param userId 用户 ID
 * @param roles 角色集合
 * @param token JWT token
 * @param expiresAt token 过期时间
 */
public record LoginResponse(
        Long userId,
        Set<String> roles,
        String token,
        Instant expiresAt
) {
}
