package com.vapor.gateway.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 后台网关启动入口。
 *
 * 统一承担后台请求的路由转发与鉴权（JWT 校验、管理员权限校验）等能力。
 */
@SpringBootApplication
public class AdminGatewayApplication {
    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AdminGatewayApplication.class, args);
    }
}
