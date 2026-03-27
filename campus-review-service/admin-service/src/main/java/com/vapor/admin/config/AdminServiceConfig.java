package com.vapor.admin.config;

import com.vapor.common.web.RequestIdFilter;
import com.vapor.common.web.UserContextFilter;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 后台服务配置。
 *
 * 提供通用 Web 过滤器与下游服务调用用的 HTTP 客户端。
 */
@Configuration
public class AdminServiceConfig {

    /**
     * 请求 ID 过滤器。
     *
     * @return 请求 ID 过滤器
     */
    @Bean
    public RequestIdFilter requestIdFilter() {
        return new RequestIdFilter();
    }

    /**
     * 用户上下文过滤器。
     *
     * @return 用户上下文过滤器
     */
    @Bean
    public UserContextFilter userContextFilter() {
        return new UserContextFilter();
    }

    /**
     * Feign 错误解码器 - 统一处理服务间调用的错误响应。
     *
     * @return 错误解码器
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}
