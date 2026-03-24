package com.vapor.model.auth;

/**
 * 刷新 Token 请求。
 *
 * @param refreshToken 刷新 Token
 */
public record RefreshTokenRequest(
        String refreshToken
) {
}
