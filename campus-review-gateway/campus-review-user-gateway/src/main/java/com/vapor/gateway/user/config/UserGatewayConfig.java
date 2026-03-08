package com.vapor.gateway.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vapor.gateway.user.filter.JwtAuthGlobalFilter;
import com.vapor.gateway.user.filter.RequestIdGlobalFilter;
import com.vapor.gateway.user.filter.SecurityHeadersGlobalFilter;
import com.vapor.utils.jwt.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * 用户侧网关配置。
 *
 * 提供 JWT 服务与全局过滤器（请求 ID、鉴权与用户上下文透传）等 Bean。
 */
@Configuration
public class UserGatewayConfig {

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
     * JWT 鉴权全局过滤器。
     *
     * @param jwtService JWT 服务
     * @param objectMapper JSON 序列化组件
     * @return JWT 鉴权过滤器
     */
    @Bean
    public JwtAuthGlobalFilter jwtAuthGlobalFilter(JwtService jwtService, ObjectMapper objectMapper) {
        return new JwtAuthGlobalFilter(jwtService, List.of(
                "/api/auth/login",
                "/api/auth/register",
                "/api/restaurants",
                "/api/rankings/hot-restaurants"
        ), objectMapper);
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
