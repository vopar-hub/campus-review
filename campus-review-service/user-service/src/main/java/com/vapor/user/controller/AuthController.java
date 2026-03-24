package com.vapor.user.controller;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.auth.LoginRequest;
import com.vapor.model.auth.LoginResponse;
import com.vapor.model.auth.RefreshTokenRequest;
import com.vapor.model.auth.RefreshTokenResponse;
import com.vapor.model.auth.RegisterRequest;
import com.vapor.model.user.UserDTO;
import com.vapor.user.service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户认证接口。
 *
 * 提供注册、登录与 Token 刷新等认证接口。
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户认证", description = "用户注册、登录等认证接口")
public class AuthController {
    private final UserAccountService userAccountService;

    /**
     * 构造控制器。
     *
     * @param userAccountService 用户账号应用服务
     */
    public AuthController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    /**
     * 注册账号并返回用户基础信息。
     *
     * @param request 注册请求
     * @return 注册成功后的用户信息
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户账号")
    public ApiResponse<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(userAccountService.register(request));
    }

    /**
     * 登录并签发 JWT。
     *
     * @param request 登录请求（账号可为邮箱或学号）
     * @return 登录结果（包含 token、refreshToken 与过期时间）
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录并签发 JWT token 和 Refresh Token")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(userAccountService.login(request));
    }

    /**
     * 刷新 Access Token。
     *
     * @param request 刷新 Token 请求
     * @return 新的 Access Token 与过期时间
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token", description = "使用 Refresh Token 刷新 Access Token")
    public ApiResponse<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(userAccountService.refreshToken(request));
    }
}
