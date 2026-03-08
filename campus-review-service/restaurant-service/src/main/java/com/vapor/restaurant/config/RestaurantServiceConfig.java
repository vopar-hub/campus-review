package com.vapor.restaurant.config;

import com.vapor.common.web.RequestIdFilter;
import com.vapor.common.web.UserContextFilter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 餐馆微服务配置。
 *
 * 提供通用 Web 过滤器 Bean，并启用 Mapper 扫描。
 */
@Configuration
@MapperScan("com.vapor.restaurant.mapper")
public class RestaurantServiceConfig {

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
}
