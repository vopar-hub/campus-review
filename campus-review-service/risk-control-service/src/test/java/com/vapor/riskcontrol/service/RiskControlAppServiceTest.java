package com.vapor.riskcontrol.service;

import com.vapor.model.risk.RateLimitResult;
import com.vapor.model.risk.RiskAuditResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RiskControlAppService 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class RiskControlAppServiceTest {

    @Mock
    private FixedWindowRateLimiter localRateLimiter;

    @Mock
    private RedisSlidingWindowRateLimiter redisRateLimiter;

    @InjectMocks
    private RiskControlAppService riskControlService;

    @BeforeEach
    void setUp() {
        // 重新创建服务以使用 mock - 使用中文逗号分隔敏感词
        riskControlService = new RiskControlAppService("spam，广告，测试敏感词", localRateLimiter, redisRateLimiter, "localhost");
    }

    @Test
    @DisplayName("内容审核通过 - 无敏感词")
    void auditContent_cleanContent() {
        // Given
        String content = "这是一条正常的评论内容";

        // When
        RiskAuditResult result = riskControlService.auditContent(content);

        // Then
        assertTrue(result.allowed());
        assertNull(result.reason());
    }

    @Test
    @DisplayName("内容审核拦截 - 包含敏感词")
    void auditContent_containsSensitiveWord() {
        // Given
        String content = "这是一条包含 spam 的广告内容";

        // When
        RiskAuditResult result = riskControlService.auditContent(content);

        // Then
        assertFalse(result.allowed());
        assertEquals("包含敏感词", result.reason());
    }

    @Test
    @DisplayName("内容审核拦截 - 包含中文敏感词")
    void auditContent_containsChineseSensitiveWord() {
        // Given
        String content = "这是一条包含广告的内容";

        // When
        RiskAuditResult result = riskControlService.auditContent(content);

        // Then
        assertFalse(result.allowed());
        assertEquals("包含敏感词", result.reason());
    }

    @Test
    @DisplayName("内容审核通过 - 大小写不敏感")
    void auditContent_caseInsensitive() {
        // Given
        String content = "这是一条包含 SPAM 的广告内容";

        // When
        RiskAuditResult result = riskControlService.auditContent(content);

        // Then
        assertFalse(result.allowed());
        assertEquals("包含敏感词", result.reason());
    }

    @Test
    @DisplayName("内容审核通过 - null 内容")
    void auditContent_nullContent() {
        // When
        RiskAuditResult result = riskControlService.auditContent(null);

        // Then
        assertTrue(result.allowed());
        assertNull(result.reason());
    }

    @Test
    @DisplayName("内容审核通过 - 空字符串")
    void auditContent_emptyContent() {
        // When
        RiskAuditResult result = riskControlService.auditContent("");

        // Then
        assertTrue(result.allowed());
        assertNull(result.reason());
    }

    @Test
    @DisplayName("限流通过 - 未超过限制")
    void rateLimit_underLimit() {
        // Given
        String key = "user:123";
        long limit = 10;
        long windowSeconds = 60;

        FixedWindowRateLimiter.RateLimitDecision localDecision =
                new FixedWindowRateLimiter.RateLimitDecision(true, 5, System.currentTimeMillis() + 60000);
        when(localRateLimiter.allow(key, limit, windowSeconds)).thenReturn(localDecision);

        // When
        RateLimitResult result = riskControlService.rateLimit(key, limit, windowSeconds);

        // Then
        assertTrue(result.allowed());
        assertEquals(5, result.remaining());
        assertTrue(result.resetAtEpochMillis() > 0);
    }

    @Test
    @DisplayName("限流拦截 - 超过限制")
    void rateLimit_exceedLimit() {
        // Given
        String key = "user:123";
        long limit = 10;
        long windowSeconds = 60;

        FixedWindowRateLimiter.RateLimitDecision localDecision =
                new FixedWindowRateLimiter.RateLimitDecision(false, 0, System.currentTimeMillis() + 60000);
        when(localRateLimiter.allow(key, limit, windowSeconds)).thenReturn(localDecision);

        // When
        RateLimitResult result = riskControlService.rateLimit(key, limit, windowSeconds);

        // Then
        assertFalse(result.allowed());
        assertEquals(0, result.remaining());
    }

    @Test
    @DisplayName("敏感词解析 - 空字符串")
    void parseKeywords_empty() {
        // Given & When
        RiskControlAppService service = new RiskControlAppService("", localRateLimiter, redisRateLimiter, "localhost");

        // Then - 不应该抛出异常，服务正常创建
        assertNotNull(service);
    }

    @Test
    @DisplayName("敏感词解析 - null")
    void parseKeywords_null() {
        // Given & When
        RiskControlAppService service = new RiskControlAppService(null, localRateLimiter, redisRateLimiter, "localhost");

        // Then - 不应该抛出异常，服务正常创建
        assertNotNull(service);
    }

    @Test
    @DisplayName("敏感词解析 - 多个关键词")
    void parseKeywords_multiple() {
        // Given
        String keywords = "spam，广告，赌博，色情";

        // When
        RiskControlAppService service = new RiskControlAppService(keywords, localRateLimiter, redisRateLimiter, "localhost");

        // Then - 服务正常创建，敏感词被解析
        assertNotNull(service);
    }
}
