package com.vapor.riskcontrol.service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 固定窗口限流器（进程内实现）。
 *
 * 以 key 维度在指定时间窗口内计数并返回是否允许，通过并发 Map 维护窗口状态。
 */
public class FixedWindowRateLimiter {
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /**
     * 判断指定 key 是否允许通过。
     *
     * @param key 限流键
     * @param limit 窗口内允许的最大次数
     * @param windowSeconds 窗口大小（秒）
     * @return 限流决策（是否允许、剩余次数、重置时间）
     */
    public RateLimitDecision allow(String key, long limit, long windowSeconds) {
        long now = Instant.now().toEpochMilli();
        long windowMs = windowSeconds * 1000L;
        Window window = windows.compute(key, (k, old) -> {
            if (old == null || now >= old.windowStartMs + windowMs) {
                return new Window(now, 1);
            }
            return new Window(old.windowStartMs, old.count + 1);
        });
        long resetAt = window.windowStartMs + windowMs;
        boolean allowed = window.count <= limit;
        long remaining = Math.max(0, limit - window.count);
        return new RateLimitDecision(allowed, remaining, resetAt);
    }

    /**
     * 固定窗口状态。
     *
     * @param windowStartMs 窗口起始时间（毫秒时间戳）
     * @param count 窗口内请求计数
     */
    private record Window(long windowStartMs, long count) {
    }

    /**
     * 限流决策结果。
     *
     * @param allowed 是否允许
     * @param remaining 当前窗口剩余可用次数
     * @param resetAtEpochMillis 窗口重置时间（毫秒时间戳）
     */
    public record RateLimitDecision(boolean allowed, long remaining, long resetAtEpochMillis) {
    }
}
