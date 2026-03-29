package com.vapor.common.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一请求日志过滤器。
 *
 * 记录每个请求的方法、路径、耗时与响应状态。
 * 支持配置开启详细日志（请求头、请求体、响应体）。
 */
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private final boolean detailedLogging;

    /**
     * 默认构造函数，关闭详细日志。
     */
    public RequestLoggingFilter() {
        this(false);
    }

    /**
     * 构造函数，指定是否开启详细日志。
     *
     * @param detailedLogging 是否开启详细日志
     */
    public RequestLoggingFilter(boolean detailedLogging) {
        this.detailedLogging = detailedLogging;
    }

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

        // 如果开启详细日志，使用包装器支持重复读取
        HttpServletRequest requestToUse = detailedLogging && !(request instanceof ContentCachingRequestWrapper)
                ? new ContentCachingRequestWrapper(request)
                : request;
        HttpServletResponse responseToUse = detailedLogging && !(response instanceof ContentCachingResponseWrapper)
                ? new ContentCachingResponseWrapper(response)
                : response;

        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullPath = queryString != null ? path + "?" + queryString : path;
        String requestId = RequestIdHolder.get();

        // 详细日志模式：记录请求详情
        if (detailedLogging && log.isDebugEnabled()) {
            log.debug("====== HTTP Request Start ======");
            log.debug("Request ID: {}", requestId);
            log.debug("Method: {}", method);
            log.debug("URI: {}", fullPath);
            log.debug("Remote IP: {}", request.getRemoteAddr());
            log.debug("Headers: {}", getHeaders(requestToUse));
            log.debug("====== HTTP Request End ======");
        }

        try {
            filterChain.doFilter(requestToUse, responseToUse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            // 基本日志记录
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

            // 详细日志模式：记录响应详情
            if (detailedLogging && log.isDebugEnabled() && responseToUse instanceof ContentCachingResponseWrapper) {
                ContentCachingResponseWrapper responseWrapper = (ContentCachingResponseWrapper) responseToUse;
                log.debug("====== HTTP Response Start ======");
                log.debug("Request ID: {}", requestId);
                log.debug("Status: {}", status);
                log.debug("Duration: {} ms", duration);
                log.debug("Response Body: {}", getResponseBody(responseWrapper));
                log.debug("====== HTTP Response End ======");

                // 复制响应体到原始响应
                responseWrapper.copyBodyToResponse();
            }
        }
    }

    /**
     * 获取请求头信息（脱敏敏感信息）。
     *
     * @param request HTTP 请求
     * @return 请求头映射
     */
    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                // 敏感信息脱敏
                if (headerName.toLowerCase().contains("authorization") ||
                    headerName.toLowerCase().contains("cookie") ||
                    headerName.toLowerCase().contains("token")) {
                    headers.put(headerName, "******");
                } else {
                    headers.put(headerName, request.getHeader(headerName));
                }
            }
        }

        return headers;
    }

    /**
     * 获取响应体。
     *
     * @param response HTTP 响应包装器
     * @return 响应体字符串
     */
    private String getResponseBody(ContentCachingResponseWrapper response) {
        try {
            byte[] content = response.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content, 0, Math.min(content.length, 10000), StandardCharsets.UTF_8);
            }
            return "";
        } catch (Exception e) {
            return "[Error reading response body: " + e.getMessage() + "]";
        }
    }
}
