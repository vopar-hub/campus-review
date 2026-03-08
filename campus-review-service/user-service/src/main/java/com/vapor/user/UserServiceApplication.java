package com.vapor.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 用户微服务启动入口。
 *
 * 提供用户注册登录、用户信息查询、后台封禁等能力。
 */
@SpringBootApplication
public class UserServiceApplication {
    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
