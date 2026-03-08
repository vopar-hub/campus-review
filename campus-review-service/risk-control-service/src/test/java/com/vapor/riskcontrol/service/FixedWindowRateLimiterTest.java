package com.vapor.riskcontrol.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FixedWindowRateLimiter 单元测试。
 */
class FixedWindowRateLimiterTest {

    private FixedWindowRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new FixedWindowRateLimiter();
    }

    @Test
    @DisplayName("首次请求允许通过")
    void allow_firstRequest() {
        // Given
        String key = "user:1";
        long limit = 5;
        long windowSeconds = 60;

        // When
        FixedWindowRateLimiter.RateLimitDecision decision = rateLimiter.allow(key, limit, windowSeconds);

        // Then
        assertTrue(decision.allowed());
        assertEquals(limit - 1, decision.remaining());
        assertNotNull(decision.resetAtEpochMillis());
    }

    @Test
    @DisplayName("在限制内允许通过")
    void allow_underLimit() {
        // Given
        String key = "user:2";
        long limit = 3;
        long windowSeconds = 60;

        // When - 前 3 次请求
        FixedWindowRateLimiter.RateLimitDecision d1 = rateLimiter.allow(key, limit, windowSeconds);
        FixedWindowRateLimiter.RateLimitDecision d2 = rateLimiter.allow(key, limit, windowSeconds);
        FixedWindowRateLimiter.RateLimitDecision d3 = rateLimiter.allow(key, limit, windowSeconds);

        // Then
        assertTrue(d1.allowed());
        assertTrue(d2.allowed());
        assertTrue(d3.allowed());
        assertEquals(0, d3.remaining());
    }

    @Test
    @DisplayName("超过限制被拦截")
    void allow_exceedLimit() {
        // Given
        String key = "user:3";
        long limit = 2;
        long windowSeconds = 60;

        // When - 前 2 次允许，第 3 次拦截
        rateLimiter.allow(key, limit, windowSeconds);
        rateLimiter.allow(key, limit, windowSeconds);
        FixedWindowRateLimiter.RateLimitDecision decision = rateLimiter.allow(key, limit, windowSeconds);

        // Then
        assertFalse(decision.allowed());
        assertEquals(0, decision.remaining());
    }

    @Test
    @DisplayName("不同 key 独立计数")
    void allow_differentKeys() {
        // Given
        long limit = 2;
        long windowSeconds = 60;

        // When - user:1 请求 2 次
        rateLimiter.allow("user:1", limit, windowSeconds);
        rateLimiter.allow("user:1", limit, windowSeconds);

        // Then - user:2 第一次请求应该允许
        FixedWindowRateLimiter.RateLimitDecision decision = rateLimiter.allow("user:2", limit, windowSeconds);
        assertTrue(decision.allowed());
        assertEquals(1, decision.remaining());
    }

    @Test
    @DisplayName("剩余次数计算正确")
    void allow_remainingCount() {
        // Given
        String key = "user:4";
        long limit = 5;
        long windowSeconds = 60;

        // When - 请求 3 次
        rateLimiter.allow(key, limit, windowSeconds);
        rateLimiter.allow(key, limit, windowSeconds);
        FixedWindowRateLimiter.RateLimitDecision decision = rateLimiter.allow(key, limit, windowSeconds);

        // Then
        assertTrue(decision.allowed());
        assertEquals(2, decision.remaining());
    }

    @Test
    @DisplayName("重置时间在未来")
    void allow_resetTimeInFuture() {
        // Given
        String key = "user:5";
        long limit = 5;
        long windowSeconds = 60;
        long beforeTest = System.currentTimeMillis();

        // When
        FixedWindowRateLimiter.RateLimitDecision decision = rateLimiter.allow(key, limit, windowSeconds);

        // Then
        long afterTest = System.currentTimeMillis();
        assertTrue(decision.resetAtEpochMillis() >= beforeTest + windowSeconds * 1000);
        assertTrue(decision.resetAtEpochMillis() <= afterTest + windowSeconds * 1000 + 1000);
    }
}
