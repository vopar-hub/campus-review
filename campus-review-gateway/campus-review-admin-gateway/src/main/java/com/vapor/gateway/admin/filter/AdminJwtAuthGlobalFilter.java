package com.vapor.gateway.admin.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vapor.gateway.admin.model.GatewayErrorResponse;
import com.vapor.utils.jwt.JwtClaims;
import com.vapor.utils.jwt.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;

/**
 * 后台 JWT 鉴权全局过滤器。
 *
 * 校验 Authorization: Bearer token，并确保包含 ADMIN 角色后才允许访问后台路由。
 */
public class AdminJwtAuthGlobalFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(AdminJwtAuthGlobalFilter.class);

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    /**
     * 构造过滤器。
     *
     * @param jwtService JWT 服务
     * @param objectMapper JSON 序列化组件
     */
    public AdminJwtAuthGlobalFilter(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    /**
     * 执行鉴权与管理员权限校验，并透传用户信息到下游服务请求头。
     *
     * @param exchange 当前请求上下文
     * @param chain 过滤器链
     * @return 响应完成信号
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            log.warn("鉴权失败 - 缺少 Authorization 头：path={}", path);
            return unauthorized(exchange);
        }

        String token = auth.substring("Bearer ".length()).trim();
        JwtClaims claims;
        try {
            claims = jwtService.parseAndValidate(token);
            log.debug("鉴权成功：userId={}, path={}", claims.userId(), path);
        } catch (ExpiredJwtException e) {
            log.info("Token 已过期：path={}", path);
            return unauthorized(exchange);
        } catch (MalformedJwtException e) {
            log.warn("Token 格式错误：path={}, error={}", path, e.getMessage());
            return unauthorized(exchange);
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的 Token 类型：path={}", path);
            return unauthorized(exchange);
        } catch (JwtException e) {
            log.warn("JWT 验证失败：path={}, error={}", path, e.getMessage());
            return unauthorized(exchange);
        } catch (IllegalArgumentException e) {
            log.warn("无效的 Token：path={}, error={}", path, e.getMessage());
            return unauthorized(exchange);
        } catch (Exception ex) {
            log.error("鉴权异常：path={}, error={}", path, ex.getMessage(), ex);
            return unauthorized(exchange);
        }

        if (!safeRoles(claims.roles()).contains("ADMIN")) {
            log.warn("无权限访问 - 非管理员：userId={}, path={}", claims.userId(), path);
            return forbidden(exchange);
        }

        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header("X-User-Id", String.valueOf(claims.userId()))
                .header("X-User-Roles", String.join(",", safeRoles(claims.roles())))
                .build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    /**
     * 返回未登录/鉴权失败响应。
     *
     * @param exchange 当前请求上下文
     * @return 响应完成信号
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        return write(exchange, HttpStatus.UNAUTHORIZED, 40100, "未登录或登录已过期");
    }

    /**
     * 返回无权限响应。
     *
     * @param exchange 当前请求上下文
     * @return 响应完成信号
     */
    private Mono<Void> forbidden(ServerWebExchange exchange) {
        return write(exchange, HttpStatus.FORBIDDEN, 40300, "无权限");
    }

    /**
     * 输出 JSON 错误响应。
     *
     * @param exchange 当前请求上下文
     * @param status HTTP 状态码
     * @param code 业务错误码
     * @param message 错误描述
     * @return 响应完成信号
     */
    private Mono<Void> write(ServerWebExchange exchange, HttpStatus status, int code, String message) {
        String requestId = exchange.getRequest().getHeaders().getFirst(RequestIdGlobalFilter.HEADER);
        GatewayErrorResponse body = new GatewayErrorResponse(
                code,
                message,
                null,
                requestId,
                Instant.now().toEpochMilli()
        );
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] bytes = objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        } catch (Exception e) {
            log.error("写入 JSON 响应失败：error={}", e.getMessage(), e);
            return exchange.getResponse().setComplete();
        }
    }

    /**
     * 规整角色集合，避免空指针。
     *
     * @param roles 角色集合
     * @return 非空角色集合
     */
    private static Set<String> safeRoles(Set<String> roles) {
        return roles == null ? Set.of() : roles;
    }

    /**
     * 过滤器执行顺序。
     *
     * @return order 值（越小越靠前）
     */
    @Override
    public int getOrder() {
        return -900;
    }
}
