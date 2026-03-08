package com.vapor.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 请求日志过滤器。
 *
 * 记录每个请求的方法、路径、耗时与响应状态，用于调试与监控。
 */
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    /**
     * 记录请求日志并在请求结束后输出耗时。
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
        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = queryString != null ? path + "?" + queryString : path;

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            String requestId = RequestIdHolder.get();

            if (status >= 500) {
                log.error("请求处理失败：requestId={}, method={}, path={}, status={}, duration={}ms",
                        requestId, method, fullPath, status, duration);
            } else if (duration > 1000) {
                log.warn("请求处理缓慢：requestId={}, method={}, path={}, status={}, duration={}ms",
                        requestId, method, fullPath, status, duration);
            } else {
                log.info("请求处理完成：requestId={}, method={}, path={}, status={}, duration={}ms",
                        requestId, method, fullPath, status, duration);
            }
        }
    }
}
