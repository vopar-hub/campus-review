package com.vapor.common.web;

import com.vapor.common.api.ApiResponse;
import com.vapor.common.error.BizException;
import com.vapor.common.error.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * 将业务异常与常见参数校验异常统一转换为 {@link ApiResponse} 输出，并附带 requestId。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常。
     *
     * @param ex 业务异常
     * @return 统一失败响应
     */
    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException ex) {
        String requestId = RequestIdHolder.get();
        log.debug("业务异常：requestId={}, errorCode={}, message={}", requestId, ex.getErrorCode(), ex.getMessage());
        return ApiResponse.fail(ex.getErrorCode(), ex.getMessage(), requestId);
    }

    /**
     * 处理参数校验与报文解析异常。
     *
     * @param ex 异常
     * @return 统一失败响应
     */
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class
    })
    public ApiResponse<Void> handleBadRequest(Exception ex) {
        String requestId = RequestIdHolder.get();
        log.debug("请求参数异常：requestId={}, error={}", requestId, ex.getMessage());
        return ApiResponse.fail(ErrorCode.BAD_REQUEST, ErrorCode.BAD_REQUEST.getMessage(), requestId);
    }

    /**
     * 处理未捕获异常。
     *
     * @param ex 异常
     * @return 统一失败响应
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex) {
        String requestId = RequestIdHolder.get();
        log.error("未捕获异常：requestId={}, error={}", requestId, ex.getMessage(), ex);
        return ApiResponse.fail(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getMessage(), requestId);
    }
}
