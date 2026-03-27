package com.vapor.admin.config;

import com.vapor.common.error.BizException;
import com.vapor.common.error.ErrorCode;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Feign 错误解码器 - 统一处理服务间调用的错误响应。
 */
public class FeignErrorDecoder implements ErrorDecoder {
    private static final Logger log = LoggerFactory.getLogger(FeignErrorDecoder.class);

    @Override
    public Exception decode(String methodKey, Response response) {
        // 读取响应体
        String body = readBody(response.body());

        log.error("Feign 调用失败：method={}, status={}, reason={}, body={}",
                  methodKey, response.status(), response.reason(), body);

        // 根据响应状态码抛出不同的业务异常
        return switch (response.status()) {
            case 400 -> new BizException(ErrorCode.BAD_REQUEST, "请求参数错误：" + body);
            case 401 -> new BizException(ErrorCode.UNAUTHORIZED, "未授权：" + body);
            case 403 -> new BizException(ErrorCode.FORBIDDEN, "无权限：" + body);
            case 404 -> new BizException(ErrorCode.NOT_FOUND, "资源不存在：" + body);
            case 429 -> new BizException(ErrorCode.TOO_MANY_REQUESTS, "请求过于频繁：" + body);
            default -> new BizException(ErrorCode.INTERNAL_ERROR, "服务调用失败 (" + response.status() + "): " + body);
        };
    }

    private String readBody(Response.Body body) {
        if (body == null) {
            return "未知错误";
        }
        try (InputStream is = body.asInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            return "读取响应体失败：" + e.getMessage();
        }
    }
}
