package com.vapor.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
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
 * HTTP 请求日志过滤器
 * 记录每个请求的详细信息，包括请求头、请求体、响应状态和耗时
 */
@Slf4j
@Component
@Order(1)
public class HttpLoggingFilter extends OncePerRequestFilter {

    private static final String START_TIME = "startTime";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 包装请求和响应以支持重复读取
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME, startTime);
        
        String requestId = MDC.get("requestId");
        String method = requestWrapper.getMethod();
        String uri = requestWrapper.getRequestURI();
        String queryString = requestWrapper.getQueryString();
        String remoteAddr = requestWrapper.getRemoteAddr();
        
        // 记录请求信息
        if (log.isDebugEnabled()) {
            log.debug("====== HTTP Request Start ======");
            log.debug("Request ID: {}", requestId);
            log.debug("Method: {}", method);
            log.debug("URI: {}", uri);
            log.debug("Query String: {}", queryString);
            log.debug("Remote IP: {}", remoteAddr);
            log.debug("Headers: {}", getHeaders(requestWrapper));
            log.debug("Request Body: {}", getRequestBody(requestWrapper));
            log.debug("====== HTTP Request End ======");
        }
        
        // 设置 MDC 用于日志追踪
        MDC.put("httpMethod", method);
        MDC.put("httpPath", uri + (queryString != null ? "?" + queryString : ""));
        MDC.put("remoteIp", remoteAddr);
        
        try {
            // 执行过滤链
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录响应信息
            if (log.isDebugEnabled()) {
                log.debug("====== HTTP Response Start ======");
                log.debug("Request ID: {}", requestId);
                log.debug("Status: {}", responseWrapper.getStatus());
                log.debug("Duration: {} ms", duration);
                log.debug("Response Body: {}", getResponseBody(responseWrapper));
                log.debug("====== HTTP Response End ======");
            }
            
            // 设置 MDC 用于 HTTP 访问日志
            MDC.put("httpStatus", String.valueOf(responseWrapper.getStatus()));
            MDC.put("httpDuration", String.valueOf(duration));
            
            // 复制响应体到原始响应
            responseWrapper.copyBodyToResponse();
            
            // 清理 MDC
            MDC.remove("httpMethod");
            MDC.remove("httpPath");
            MDC.remove("httpStatus");
            MDC.remove("httpDuration");
            MDC.remove("remoteIp");
        }
    }

    /**
     * 获取请求头信息
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
     * 获取请求体
     */
    private String getRequestBody(ContentCachingRequestWrapper request) {
        try {
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content, 0, Math.min(content.length, 10000), StandardCharsets.UTF_8);
            }
            return "";
        } catch (Exception e) {
            return "[Error reading request body: " + e.getMessage() + "]";
        }
    }

    /**
     * 获取响应体
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
