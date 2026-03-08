package com.vapor.gateway.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 用户侧网关启动入口。
 *
 * 统一承担用户端请求的路由转发与鉴权（JWT 校验、用户上下文透传）等能力。
 */
@SpringBootApplication
public class UserGatewayApplication {
    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(UserGatewayApplication.class, args);
    }
}
