package com.vapor.common.error;

import lombok.Getter;

/**
 * 业务异常。
 *
 * 通过携带 {@link ErrorCode} 在全局异常处理中转换为统一响应体。
 */
@Getter
public class BizException extends RuntimeException {
    /**
     * -- GETTER --
     *  获取错误码。
     *
     * @return 错误码
     */
    private final ErrorCode errorCode;

    /**
     * 构造业务异常，使用错误码的默认 message。
     *
     * @param errorCode 错误码
     */
    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 构造业务异常，使用自定义 message。
     *
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
