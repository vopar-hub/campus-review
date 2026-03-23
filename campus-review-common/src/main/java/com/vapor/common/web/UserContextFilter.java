package com.vapor.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servlet 用户上下文过滤器。
 *
 * 从请求头读取用户信息并写入 {@link UserContextHolder}，在请求结束后清理线程上下文。
 */
@Slf4j
public class UserContextFilter extends OncePerRequestFilter {
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String USER_ROLES_HEADER = "X-User-Roles";

    /**
     * 解析用户上下文并透传到当前线程。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestId = request.getHeader("X-Request-Id");
        String userIdRaw = request.getHeader(USER_ID_HEADER);
        String rolesRaw = request.getHeader(USER_ROLES_HEADER);
        
        log.info("[UserContextFilter] 请求进入 - requestId={}, path={}, method={}, userIdHeader={}, rolesHeader={}", 
                requestId, request.getRequestURI(), request.getMethod(), userIdRaw, rolesRaw);
        
        if (userIdRaw != null && !userIdRaw.isBlank()) {
            try {
                Long userId = Long.parseLong(userIdRaw);
                Set<String> roles = rolesRaw == null || rolesRaw.isBlank()
                        ? Set.of()
                        : Arrays.stream(rolesRaw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toSet());
                UserContextHolder.set(new UserContext(userId, roles));
                log.info("[UserContextFilter] 用户上下文已设置 - requestId={}, userId={}, roles={}", requestId, userId, roles);
            } catch (NumberFormatException e) {
                log.warn("[UserContextFilter] 用户 ID 格式错误 - requestId={}, userIdRaw={}", requestId, userIdRaw, e);
            }
        } else {
            log.warn("[UserContextFilter] 缺少用户 ID 请求头 - requestId={}, userIdRaw={}", requestId, userIdRaw);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
            log.debug("[UserContextFilter] 用户上下文已清理 - requestId={}", requestId);
        }
    }
}
