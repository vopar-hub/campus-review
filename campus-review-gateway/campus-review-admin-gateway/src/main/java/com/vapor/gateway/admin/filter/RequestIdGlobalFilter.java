package com.vapor.gateway.admin.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 请求 ID 全局过滤器。
 *
 * 为每个请求生成或复用 X-Request-Id，并同时写入响应头与下游请求头，便于链路追踪。
 */
public class RequestIdGlobalFilter implements GlobalFilter, Ordered {
    public static final String HEADER = "X-Request-Id";

    /**
     * 注入请求 ID。
     *
     * @param exchange 当前请求上下文
     * @param chain 过滤器链
     * @return 响应完成信号
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst(HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        exchange.getResponse().getHeaders().set(HEADER, requestId);
        ServerHttpRequest mutated = exchange.getRequest().mutate().header(HEADER, requestId).build();
        return chain.filter(exchange.mutate().request(mutated).build());
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
