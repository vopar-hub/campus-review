package com.vapor.common.web;

import java.util.Collections;
import java.util.Set;

/**
 * 用户上下文。
 *
 * 由网关或上游系统透传用户信息后，在服务端线程内保存当前请求的用户身份与角色集合。
 */
public class UserContext {
    private final Long userId;
    private final Set<String> roles;

    /**
     * 构造用户上下文。
     *
     * @param userId 用户 ID
     * @param roles 角色集合
     */
    public UserContext(Long userId, Set<String> roles) {
        this.userId = userId;
        this.roles = roles == null ? Collections.emptySet() : Collections.unmodifiableSet(roles);
    }

    /**
     * 获取用户 ID。
     *
     * @return 用户 ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 获取角色集合。
     *
     * @return 角色集合（只读）
     */
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * 判断是否拥有指定角色。
     *
     * @param role 角色名
     * @return 是否拥有
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
