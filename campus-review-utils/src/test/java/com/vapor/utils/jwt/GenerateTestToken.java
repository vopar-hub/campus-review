package com.vapor.utils.jwt;

import java.time.Duration;
import java.util.Set;

/**
 * 生成测试 Token
 */
public class GenerateTestToken {
    public static void main(String[] args) {
        String secret = "my-super-secret-key-for-jwt-token-must-be-long-enough";
        JwtService jwtService = new JwtService(secret, Duration.ofDays(1));

        // 生成管理员 Token (用户 ID = 7)
        String adminToken = jwtService.issue(7L, Set.of("ADMIN"));
        System.out.println("ADMIN Token (userId=7):");
        System.out.println(adminToken);
        System.out.println();

        // 生成普通用户 Token (用户 ID = 1)
        String userToken = jwtService.issue(1L, Set.of("USER"));
        System.out.println("USER Token (userId=1):");
        System.out.println(userToken);
    }
}
