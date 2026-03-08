package com.vapor.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 评价微服务启动入口。
 *
 * 提供评价发布、查询与后台审核等能力。
 */
@SpringBootApplication(scanBasePackages = "com.vapor")
public class ReviewServiceApplication {
    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);
    }
}
