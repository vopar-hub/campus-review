package com.vapor.common.api;

import com.vapor.common.error.ErrorCode;

import java.time.Instant;

/**
 * 统一 API 响应体。
 *
 * @param <T> data 字段的数据类型
 */
public class ApiResponse<T> {
    private final int code;
    private final String message;
    private final T data;
    private final String requestId;
    private final long timestamp;

    /**
     * 构造响应体。
     *
     * @param code 业务码
     * @param message 提示信息
     * @param data 响应数据
     * @param requestId 请求 ID
     * @param timestamp 时间戳（毫秒）
     */
    public ApiResponse(int code, String message, T data, String requestId, long timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.requestId = requestId;
        this.timestamp = timestamp;
    }

    /**
     * 构造成功响应（不带 requestId）。
     *
     * @param data 响应数据
     * @return 成功响应
     * @param <T> data 的类型
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(ErrorCode.OK.getCode(), ErrorCode.OK.getMessage(), data, null, Instant.now().toEpochMilli());
    }

    /**
     * 构造成功响应（带 requestId）。
     *
     * @param data 响应数据
     * @param requestId 请求 ID
     * @return 成功响应
     * @param <T> data 的类型
     */
    public static <T> ApiResponse<T> ok(T data, String requestId) {
        return new ApiResponse<>(ErrorCode.OK.getCode(), ErrorCode.OK.getMessage(), data, requestId, Instant.now().toEpochMilli());
    }

    /**
     * 构造失败响应。
     *
     * @param errorCode 错误码
     * @param message 错误信息
     * @param requestId 请求 ID
     * @return 失败响应
     * @param <T> data 的类型
     */
    public static <T> ApiResponse<T> fail(ErrorCode errorCode, String message, String requestId) {
        return new ApiResponse<>(errorCode.getCode(), message, null, requestId, Instant.now().toEpochMilli());
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
     * 获取提示信息。
     *
     * @return 提示信息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取响应数据。
     *
     * @return 响应数据
     */
    public T getData() {
        return data;
    }

    /**
     * 获取请求 ID。
     *
     * @return 请求 ID
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * 获取时间戳（毫秒）。
     *
     * @return 时间戳
     */
    public long getTimestamp() {
        return timestamp;
    }
}
