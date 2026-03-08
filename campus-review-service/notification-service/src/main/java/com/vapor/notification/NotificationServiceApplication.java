package com.vapor.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 通知微服务启动入口。
 *
 * 提供站内消息投递、查询与已读标记等能力。
 */
@SpringBootApplication(scanBasePackages = "com.vapor")
public class NotificationServiceApplication {
    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
