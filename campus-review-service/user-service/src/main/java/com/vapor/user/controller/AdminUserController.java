package com.vapor.user.controller;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.user.UserDTO;
import com.vapor.user.service.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户后台管理接口（供后台网关转发）。
 *
 * 主要用于用户封禁与解封等管理动作。
 */
@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "用户管理", description = "用户封禁、解封等管理接口")
public class AdminUserController {
    private final UserAdminService userAdminService;

    /**
     * 构造控制器。
     *
     * @param userAdminService 用户后台管理应用服务
     */
    public AdminUserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    /**
     * 获取用户列表。
     *
     * @return 用户列表
     */
    @GetMapping
    @Operation(summary = "获取用户列表", description = "获取所有用户列表")
    public ApiResponse<List<UserDTO>> getUserList() {
        return ApiResponse.ok(userAdminService.getUserList());
    }

    /**
     * 封禁指定用户。
     *
     * @param id 用户 ID
     * @return 空响应体
     */
    @PostMapping("/{id}/ban")
    @Operation(summary = "封禁用户", description = "封禁指定用户账号")
    public ApiResponse<Void> ban(@PathVariable Long id) {
        userAdminService.ban(id);
        return ApiResponse.ok(null);
    }

    /**
     * 解封指定用户。
     *
     * @param id 用户 ID
     * @return 空响应体
     */
    @PostMapping("/{id}/unban")
    @Operation(summary = "解封用户", description = "解封指定用户账号")
    public ApiResponse<Void> unban(@PathVariable Long id) {
        userAdminService.unban(id);
        return ApiResponse.ok(null);
    }
}
