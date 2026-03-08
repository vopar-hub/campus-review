package com.vapor.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet 请求 ID 过滤器。
 *
 * 为每个请求生成或复用 X-Request-Id，并写入 {@link RequestIdHolder} 与响应头。
 */
public class RequestIdFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Request-Id";

    /**
     * 注入 requestId 并保证在请求结束后清理线程上下文。
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
        String requestId = request.getHeader(HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        RequestIdHolder.set(requestId);
        response.setHeader(HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            RequestIdHolder.clear();
        }
    }
}
