package com.vapor.model.user;

import jakarta.validation.constraints.Size;

/**
 * 更新用户信息请求体。
 *
 * 支持部分更新，所有字段均为可选。
 *
 * @param nickname 昵称（1-50字符），可选
 * @param avatarUrl 头像URL（最大500字符），可选
 */
public record UpdateUserRequest(
        @Size(min = 1, max = 50, message = "昵称长度需在1-50字符之间") String nickname,
        @Size(max = 500, message = "头像URL长度不能超过500字符") String avatarUrl
) {
}
