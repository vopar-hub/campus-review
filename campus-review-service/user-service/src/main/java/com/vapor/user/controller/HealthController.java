package com.vapor.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查接口。
 *
 * 提供服务的健康状态检查能力，用于负载均衡与容器编排。
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "健康检查", description = "服务健康状态检查接口")
public class HealthController {

    /**
     * 服务健康检查。
     *
     * @return 健康状态信息
     */
    @GetMapping
    @Operation(summary = "健康检查", description = "检查服务是否正常运行")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", Instant.now().toString());
        result.put("service", "user-service");
        return result;
    }

    /**
     * 服务就绪检查。
     *
     * @return 就绪状态信息
     */
    @GetMapping("/ready")
    @Operation(summary = "就绪检查", description = "检查服务是否已就绪可接收请求")
    public Map<String, Object> ready() {
        Map<String, Object> result = new HashMap<>();
        result.put("ready", true);
        result.put("timestamp", Instant.now().toString());
        return result;
    }
}
