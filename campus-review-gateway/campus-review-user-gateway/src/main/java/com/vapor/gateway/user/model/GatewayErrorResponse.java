package com.vapor.gateway.user.model;

/**
 * 网关错误响应模型。
 *
 * 用于网关层统一输出鉴权/权限等错误信息，并携带 requestId 便于排查。
 */
public record GatewayErrorResponse(
        int code,
        String message,
        Object data,
        String requestId,
        long timestamp
) {
}
