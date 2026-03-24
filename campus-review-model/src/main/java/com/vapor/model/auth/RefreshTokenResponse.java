package com.vapor.model.auth;

import java.time.Instant;

/**
 * 刷新 Token 响应。
 *
 * @param token 新的 Access Token
 * @param expiresAt Access Token 过期时间
 */
public record RefreshTokenResponse(
        String token,
        Instant expiresAt
) {
}
