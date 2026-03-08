package com.vapor.riskcontrol.config;

import com.vapor.common.web.RequestIdFilter;
import com.vapor.common.web.UserContextFilter;
import com.vapor.riskcontrol.service.FixedWindowRateLimiter;
import com.vapor.riskcontrol.service.RedisSlidingWindowRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 风控微服务配置。
 *
 * 提供通用 Web 过滤器 Bean。
 */
@Configuration
public class RiskControlServiceConfig {

    /**
     * 请求 ID 过滤器。
     *
     * @return 请求 ID 过滤器
     */
    @Bean
    public RequestIdFilter requestIdFilter() {
        return new RequestIdFilter();
    }

    /**
     * 用户上下文过滤器。
     *
     * @return 用户上下文过滤器
     */
    @Bean
    public UserContextFilter userContextFilter() {
        return new UserContextFilter();
    }

    /**
     * 本地固定窗口限流器（作为 Redis 不可用时的备用）。
     *
     * @return FixedWindowRateLimiter
     */
    @Bean
    public FixedWindowRateLimiter fixedWindowRateLimiter() {
        return new FixedWindowRateLimiter();
    }

    /**
     * 基于 Redis 的滑动窗口限流器。
     *
     * @param redisTemplate StringRedisTemplate
     * @return RedisSlidingWindowRateLimiter
     */
    @Bean
    public RedisSlidingWindowRateLimiter redisSlidingWindowRateLimiter(StringRedisTemplate redisTemplate) {
        return new RedisSlidingWindowRateLimiter(redisTemplate);
    }
}
