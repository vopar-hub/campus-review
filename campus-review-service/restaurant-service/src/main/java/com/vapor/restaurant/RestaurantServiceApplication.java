package com.vapor.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 餐馆微服务启动入口。
 *
 * 提供餐馆创建、查询与检索等能力。
 */
@SpringBootApplication(scanBasePackages = "com.vapor")
public class RestaurantServiceApplication {
    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }
}
