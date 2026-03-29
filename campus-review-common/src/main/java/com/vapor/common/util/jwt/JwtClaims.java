package com.vapor.common.util.jwt;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT 解析结果。
 *
 * @param userId 用户 ID
 * @param roles 角色集合
 * @param expiresAt 过期时间
 */
public record JwtClaims(Long userId, Set<String> roles, Instant expiresAt) {
    /**
     * 将逗号分隔的角色字符串解析为集合。
     *
     * @param rolesCsv 角色字符串（逗号分隔）
     * @return 角色集合
     */
    public static Set<String> parseRoles(String rolesCsv) {
        if (rolesCsv == null || rolesCsv.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(rolesCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }
}
