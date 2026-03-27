package com.vapor.ranking.config;

import com.vapor.common.error.BizException;
import com.vapor.common.error.ErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Feign 错误解码器 - 统一处理服务间调用的错误响应。
 */
public class FeignErrorDecoder implements ErrorDecoder {
    private static final Logger log = LoggerFactory.getLogger(FeignErrorDecoder.class);

    @Override
    public Exception decode(String methodKey, Response response) {
        // 读取响应体
        String body = response.body() != null ? new String(response.body().asByteArray()) : "未知错误";

        log.error("Feign 调用失败：method={}, status={}, reason={}, body={}",
                  methodKey, response.status(), response.reason(), body);

        // 根据响应状态码抛出不同的业务异常
        return switch (response.status()) {
            case 400 -> new BizException(ErrorCode.BAD_REQUEST, "请求参数错误：" + body);
            case 401 -> new BizException(ErrorCode.UNAUTHORIZED, "未授权：" + body);
            case 403 -> new BizException(ErrorCode.FORBIDDEN, "无权限：" + body);
            case 404 -> new BizException(ErrorCode.NOT_FOUND, "资源不存在：" + body);
            case 409 -> new BizException(ErrorCode.CONFLICT, "资源冲突：" + body);
            case 422 -> new BizException(ErrorCode.VALIDATION_ERROR, "参数校验失败：" + body);
            case 500 -> new BizException(ErrorCode.INTERNAL_SERVER_ERROR, "服务端错误：" + body);
            case 502 -> new BizException(ErrorCode.INTERNAL_SERVER_ERROR, "网关错误：" + body);
            case 503 -> new BizException(ErrorCode.INTERNAL_SERVER_ERROR, "服务不可用：" + body);
            case 504 -> new BizException(ErrorCode.INTERNAL_SERVER_ERROR, "网关超时：" + body);
            default -> new BizException(ErrorCode.UNKNOWN, "服务调用失败 (" + response.status() + "): " + body);
        };
    }
}
