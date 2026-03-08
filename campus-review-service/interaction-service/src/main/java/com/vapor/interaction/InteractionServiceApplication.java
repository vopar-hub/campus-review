package com.vapor.interaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 互动微服务启动入口。
 *
 * 提供点赞与收藏等互动能力，并对外提供互动计数查询。
 */
@SpringBootApplication(scanBasePackages = "com.vapor")
public class InteractionServiceApplication {
    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(InteractionServiceApplication.class, args);
    }
}
