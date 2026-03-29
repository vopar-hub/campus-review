package com.vapor.gateway.admin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vapor.gateway.admin.filter.AdminJwtAuthGlobalFilter;
import com.vapor.gateway.admin.filter.RequestIdGlobalFilter;
import com.vapor.gateway.admin.filter.SecurityHeadersGlobalFilter;
import com.vapor.common.util.jwt.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 后台网关配置。
 *
 * 提供 JWT 服务与全局过滤器（请求 ID、后台鉴权）等 Bean。
 */
@Configuration
public class AdminGatewayConfig {

    /**
     * JWT 服务组件。
     *
     * @param secret JWT 密钥
     * @return JWT 服务
     */
    @Bean
    public JwtService jwtService(@Value("${security.jwt.secret}") String secret) {
        return new JwtService(secret, Duration.ofSeconds(86400));
    }

    /**
     * 请求 ID 全局过滤器。
     *
     * @return 请求 ID 过滤器
     */
    @Bean
    public RequestIdGlobalFilter requestIdGlobalFilter() {
        return new RequestIdGlobalFilter();
    }

    /**
     * 后台 JWT 鉴权全局过滤器。
     *
     * @param jwtService JWT 服务
     * @param objectMapper JSON 序列化组件
     * @return 后台鉴权过滤器
     */
    @Bean
    public AdminJwtAuthGlobalFilter adminJwtAuthGlobalFilter(JwtService jwtService, ObjectMapper objectMapper) {
        return new AdminJwtAuthGlobalFilter(jwtService, objectMapper);
    }

    /**
     * 安全响应头全局过滤器。
     *
     * @return 安全响应头过滤器
     */
    @Bean
    public SecurityHeadersGlobalFilter securityHeadersGlobalFilter() {
        return new SecurityHeadersGlobalFilter();
    }
}
