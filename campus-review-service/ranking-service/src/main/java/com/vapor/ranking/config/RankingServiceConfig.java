package com.vapor.ranking.config;

import com.vapor.common.web.RequestIdFilter;
import com.vapor.common.web.UserContextFilter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * 排行榜微服务配置。
 *
 * 提供通用 Web 过滤器与下游服务调用用的 HTTP 客户端，并启用 Mapper 扫描。
 */
@Configuration
@MapperScan("com.vapor.ranking.mapper")
@EnableCaching
public class RankingServiceConfig {

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
     * 下游服务调用用 RestClient。
     *
     * @param builder RestClient 构建器
     * @return RestClient
     */
    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }

    /**
     * Redis 缓存管理器。
     *
     * @param factory Redis 连接工厂
     * @return 缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer(
                        org.springframework.data.redis.serializer.RedisSerializer.string()))
                .serializeValuesWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer(
                        org.springframework.data.redis.serializer.RedisSerializer.json()));
        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .withCacheConfiguration("ranking:hot-restaurants", config.entryTtl(Duration.ofMinutes(5)))
                .build();
    }
}
