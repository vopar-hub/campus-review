package com.vapor.model.user;

import java.time.Instant;
import java.util.Set;

/**
 * 用户信息 DTO。
 *
 * @param id 用户 ID
 * @param email 邮箱
 * @param studentNo 学号
 * @param nickname 昵称
 * @param avatarUrl 头像URL
 * @param roles 角色集合
 * @param banned 是否封禁
 * @param createdAt 创建时间
 */
public record UserDTO(
        Long id,
        String email,
        String studentNo,
        String nickname,
        String avatarUrl,
        Set<String> roles,
        boolean banned,
        Instant createdAt
) {
}
