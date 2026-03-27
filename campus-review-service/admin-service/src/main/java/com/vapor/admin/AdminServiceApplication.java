package com.vapor.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 后台聚合微服务启动入口。
 *
 * 提供后台统一管理 API，并通过编排方式调用用户/评价等下游微服务完成管理动作。
 */
@SpringBootApplication(scanBasePackages = "com.vapor")
@EnableFeignClients(basePackages = "com.vapor.admin.client")
//@EnableDiscoveryClient
public class AdminServiceApplication {


    /**
     * Spring Boot 启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {


        SpringApplication.run(AdminServiceApplication.class, args);

        System.out.println("哈哈哈,启动成功了");


    }
}
