package com.vapor.ranking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 排行榜微服务启动入口。
 *
 * 启用定时调度，用于周期性刷新排行榜数据。
 */
@SpringBootApplication(scanBasePackages = "com.vapor")
@EnableScheduling
public class RankingServiceApplication {
    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(RankingServiceApplication.class, args);
    }
}
