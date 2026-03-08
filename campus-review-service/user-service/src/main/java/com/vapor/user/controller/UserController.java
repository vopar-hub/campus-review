package com.vapor.user.controller;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.user.UserDTO;
import com.vapor.user.service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户侧用户信息接口。
 *
 * 通过用户上下文返回当前登录用户的信息。
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户信息", description = "当前用户信息查询接口")
public class UserController {
    private final UserAccountService userAccountService;

    /**
     * 构造控制器。
     *
     * @param userAccountService 用户账号与用户信息应用服务
     */
    public UserController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    /**
     * 获取当前登录用户信息。
     *
     * @return 当前用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public ApiResponse<UserDTO> me() {
        return ApiResponse.ok(userAccountService.me());
    }
}
