package com.vapor.common.error;

/**
 * 统一错误码定义。
 *
 * 约定：code 为业务码，message 为默认对外提示信息。
 */
public enum ErrorCode {
    OK(0, "OK"),
    BAD_REQUEST(40000, "请求参数错误"),
    UNAUTHORIZED(40100, "未登录或登录已过期"),
    FORBIDDEN(40300, "无权限"),
    NOT_FOUND(40400, "资源不存在"),
    TOO_MANY_REQUESTS(42900, "请求过于频繁"),
    INTERNAL_ERROR(50000, "服务器内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取业务码。
     *
     * @return 业务码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取默认提示信息。
     *
     * @return 默认提示信息
     */
    public String getMessage() {
        return message;
    }
}
