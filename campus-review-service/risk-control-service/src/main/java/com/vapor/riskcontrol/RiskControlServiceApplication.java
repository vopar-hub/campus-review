package com.vapor.riskcontrol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 风控微服务启动入口。
 *
 * 提供内容审核与限流判定等基础风控能力。
 */
@SpringBootApplication(scanBasePackages = "com.vapor")
public class RiskControlServiceApplication {
    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(RiskControlServiceApplication.class, args);
    }
}
