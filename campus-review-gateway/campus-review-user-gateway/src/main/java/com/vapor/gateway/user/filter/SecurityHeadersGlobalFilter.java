package com.vapor.gateway.user.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 安全响应头全局过滤器。
 *
 * 添加常见的安全响应头，增强应用安全性。
 */
public class SecurityHeadersGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 添加安全响应头。
     *
     * @param exchange 当前请求上下文
     * @param chain 过滤器链
     * @return 响应完成信号
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 防止点击劫持
        response.getHeaders().add("X-Frame-Options", "DENY");

        // 启用 XSS 防护
        response.getHeaders().add("X-XSS-Protection", "1; mode=block");

        // 防止 MIME 类型嗅探
        response.getHeaders().add("X-Content-Type-Options", "nosniff");

        // 内容安全策略（宽松版本，允许同源资源）
        response.getHeaders().add("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self' data:;");

        // Referrer 策略
        response.getHeaders().add("Referrer-Policy", "strict-origin-when-cross-origin");

        // 权限策略
        response.getHeaders().add("Permissions-Policy",
                "geolocation=(), microphone=(), camera=()");

        // 缓存控制（针对 API 响应）
        if (request.getURI().getPath().startsWith("/api/")) {
            response.getHeaders().add("Cache-Control", "no-store, no-cache, must-revalidate");
            response.getHeaders().add("Pragma", "no-cache");
            response.getHeaders().add("Expires", "0");
        }

        return chain.filter(exchange);
    }

    /**
     * 过滤器执行顺序。
     *
     * @return order 值（越小越靠前）
     */
    @Override
    public int getOrder() {
        return -1000;
    }
}
