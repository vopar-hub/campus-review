package com.vapor.admin.client;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.user.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * 用户服务 Feign 客户端。
 */
@FeignClient(name = "user-service")
public interface UserServiceClient {

    /**
     * 获取用户列表。
     *
     * @return 用户列表响应
     */
    @GetMapping("/api/admin/users")
    ApiResponse<List<UserDTO>> getUsers();

    /**
     * 封禁指定用户。
     *
     * @param userId 用户 ID
     * @return 空响应
     */
    @PostMapping("/api/admin/users/{userId}/ban")
    ApiResponse<Void> banUser(@PathVariable("userId") Long userId);

    /**
     * 解封指定用户。
     *
     * @param userId 用户 ID
     * @return 空响应
     */
    @PostMapping("/api/admin/users/{userId}/unban")
    ApiResponse<Void> unbanUser(@PathVariable("userId") Long userId);
}
