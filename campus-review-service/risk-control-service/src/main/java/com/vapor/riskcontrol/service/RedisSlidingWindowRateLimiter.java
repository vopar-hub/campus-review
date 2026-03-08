package com.vapor.riskcontrol.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.List;

/**
 * 滑动窗口限流器（Redis 分布式实现）。
 *
 * 使用 Redis ZSet 实现滑动窗口限流，支持分布式环境下的精确限流控制。
 */
public class RedisSlidingWindowRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RedisSlidingWindowRateLimiter.class);

    /**
     * Redis Lua 脚本：原子性地执行滑动窗口限流检查。
     * 脚本逻辑：
     * 1. 移除窗口外的旧记录
     * 2. 统计当前窗口内的记录数
     * 3. 如果未超过限制，添加新记录并设置过期时间
     */
    private static final String LUA_SCRIPT = """
        local key = KEYS[1]
        local now = tonumber(ARGV[1])
        local windowMs = tonumber(ARGV[2])
        local limit = tonumber(ARGV[3])
        local windowStart = now - windowMs

        -- 移除窗口外的旧记录
        redis.call('ZREMRANGEBYSCORE', key, '-inf', windowStart)

        -- 统计当前窗口内的记录数
        local count = redis.call('ZCARD', key)

        -- 检查是否超过限制
        if count < limit then
            -- 未超限，添加新记录
            redis.call('ZADD', key, now, now .. '-' .. math.random(1, 1000000))
            redis.call('EXPIRE', key, math.ceil(windowMs / 1000) + 1)
            return {1, limit - count - 1}  -- 允许，返回剩余次数
        else
            -- 超限，返回当前剩余次数（0）
            return {0, 0}
        end
        """;

    private final RedisScript<List<Long>> redisScript;
    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    /**
     * 构造限流器。
     *
     * @param redisTemplate Redis StringRedisTemplate
     */
    @SuppressWarnings("unchecked")
    public RedisSlidingWindowRateLimiter(org.springframework.data.redis.core.StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisScript = (RedisScript<List<Long>>) (RedisScript<?>) new DefaultRedisScript<>(LUA_SCRIPT, List.class);
    }

    /**
     * 判断指定 key 是否允许通过。
     *
     * @param key 限流键（如：user:123, ip:192.168.1.1）
     * @param limit 窗口内允许的最大次数
     * @param windowSeconds 窗口大小（秒）
     * @return 限流决策（是否允许、剩余次数）
     */
    public RateLimitDecision allow(String key, long limit, long windowSeconds) {
        long now = System.currentTimeMillis();
        long windowMs = windowSeconds * 1000L;
        try {
            String redisKey = "rate_limit:" + key;

            List<Long> result = redisTemplate.execute(
                    redisScript,
                    Collections.singletonList(redisKey),
                    String.valueOf(now),
                    String.valueOf(windowMs),
                    String.valueOf(limit)
            );

            if (result != null && result.size() >= 2) {
                boolean allowed = result.get(0) == 1;
                long remaining = result.get(1);
                long resetAt = now + windowMs;
                return new RateLimitDecision(allowed, remaining, resetAt);
            }

            // 脚本执行异常时，默认允许通过（fail-open）
            log.warn("Redis 限流脚本执行失败，默认允许通过：key={}", key);
            return new RateLimitDecision(true, limit, now + windowMs);

        } catch (Exception e) {
            log.error("Redis 限流检查异常：key={}, error={}", key, e.getMessage(), e);
            // 异常时默认允许通过（fail-open），避免影响正常业务
            return new RateLimitDecision(true, limit, now + windowMs);
        }
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
