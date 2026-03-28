package com.vapor.restaurant;

import com.vapor.restaurant.service.MinioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 餐馆微服务启动入口。
 *
 * 提供餐馆创建、查询与检索等能力。
 */
@SpringBootApplication(scanBasePackages = "com.vapor")
@EnableScheduling
public class RestaurantServiceApplication {
    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }

    /**
     * 应用启动后初始化 MinIO 存储桶。
     *
     * @param minioService MinIO 服务
     * @return CommandLineRunner
     */
    @Bean
    public CommandLineRunner initMinioBucket(MinioService minioService) {
        return args -> minioService.initBucket();
    }
}
