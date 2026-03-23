package com.vapor.gateway.user.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vapor.gateway.user.model.GatewayErrorResponse;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * 用户侧 JWT 鉴权全局过滤器。
 *
 * 对非白名单请求校验 Authorization: Bearer token，并将用户信息透传到下游服务请求头中。
 */
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthGlobalFilter.class);

    private final JwtService jwtService;
    private final List<String> exactWhitelist;
    private final ObjectMapper objectMapper;

    /**
     * 构造过滤器。
     *
     * @param jwtService JWT 服务
     * @param exactWhitelist 精确匹配白名单路径
     * @param objectMapper JSON 序列化组件
     */
    public JwtAuthGlobalFilter(JwtService jwtService, List<String> exactWhitelist, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.exactWhitelist = exactWhitelist;
        this.objectMapper = objectMapper;
    }

    /**
     * 执行鉴权与用户上下文透传。
     *
     * @param exchange 当前请求上下文
     * @param chain 过滤器链
     * @return 响应完成信号
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String requestId = exchange.getRequest().getHeaders().getFirst(RequestIdGlobalFilter.HEADER);

        if (shouldSkip(exchange)) {
            log.debug("跳过鉴权：requestId={}, path={}", requestId, path);
            return chain.filter(exchange);
        }

        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            log.warn("鉴权失败 - 缺少 Authorization 头：requestId={}, path={}", requestId, path);
            return unauthorized(exchange);
        }

        String token = auth.substring("Bearer ".length()).trim();
        log.info("[JwtAuthGlobalFilter] 开始 JWT 鉴权 - requestId={}, path={}, tokenPrefix={}", 
                requestId, path, token.length() > 20 ? token.substring(0, 20) + "..." : token);

        
        try {
            JwtClaims claims = jwtService.parseAndValidate(token);
            log.info("[JwtAuthGlobalFilter] 鉴权成功 - requestId={}, userId={}, roles={}", 
                    requestId, claims.userId(), claims.roles());
            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(claims.userId()))
                    .header("X-User-Roles", String.join(",", safeRoles(claims.roles())))
                    .build();


            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (ExpiredJwtException e) {
            log.info("Token 已过期：requestId={}, path={}", requestId, path);
            return unauthorized(exchange);
        } catch (MalformedJwtException e) {
            log.warn("Token 格式错误：requestId={}, path={}, error={}", requestId, path, e.getMessage());
            return unauthorized(exchange);
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的 Token 类型：requestId={}, path={}", requestId, path);
            return unauthorized(exchange);
        } catch (JwtException e) {
            log.warn("JWT 验证失败：requestId={}, path={}, error={}", requestId, path, e.getMessage());
            return unauthorized(exchange);
        } catch (IllegalArgumentException e) {
            log.warn("无效的 Token：requestId={}, path={}, error={}", requestId, path, e.getMessage());
            return unauthorized(exchange);
        } catch (Exception ex) {
            log.error("鉴权异常：requestId={}, path={}, error={}", requestId, path, ex.getMessage(), ex);
            return unauthorized(exchange);
        }
    }

    /**
     * 判断当前请求是否跳过鉴权。
     *
     * @param exchange 当前请求上下文
     * @return 是否跳过
     */
    private boolean shouldSkip(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        if (exactWhitelist.contains(path)) {
            return true;
        }
        if (path.startsWith("/api/auth/")) {
            return true;
        }
        HttpMethod method = exchange.getRequest().getMethod();
        if (HttpMethod.GET.equals(method) && path.startsWith("/api/restaurants")) {
            return true;
        }
        if (HttpMethod.GET.equals(method) && path.startsWith("/api/rankings")) {
            return true;
        }
        return false;
    }

    /**
     * 返回未登录/鉴权失败响应。
     *
     * @param exchange 当前请求上下文
     * @return 响应完成信号
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        String requestId = exchange.getRequest().getHeaders().getFirst(RequestIdGlobalFilter.HEADER);
        GatewayErrorResponse body = new GatewayErrorResponse(
                40100,
                "未登录或登录已过期",
                null,
                requestId,
                Instant.now().toEpochMilli()
        );
        return writeJson(exchange, HttpStatus.UNAUTHORIZED, body);
    }

    /**
     * 输出 JSON 响应。
     *
     * @param exchange 当前请求上下文
     * @param status HTTP 状态码
     * @param body 响应体
     * @return 响应完成信号
     */
    private Mono<Void> writeJson(ServerWebExchange exchange, HttpStatus status, Object body) {
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
        return 2;
    }
}
