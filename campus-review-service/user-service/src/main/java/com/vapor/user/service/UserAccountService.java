package com.vapor.user.service;

import com.vapor.model.auth.LoginRequest;
import com.vapor.model.auth.LoginResponse;
import com.vapor.model.auth.RefreshTokenRequest;
import com.vapor.model.auth.RefreshTokenResponse;
import com.vapor.model.auth.RegisterRequest;
import com.vapor.model.user.UserDTO;

/**
 * 用户账号服务接口。
 *
 * 负责注册、登录（签发 JWT）以及基于用户上下文的个人信息查询等核心流程。
 */
public interface UserAccountService {

    /**
     * 注册新用户。
     *
     * @param request 注册请求
     * @return 注册成功后的用户信息
     */
    UserDTO register(RegisterRequest request);

    /**
     * 用户登录并签发 JWT。
     *
     * @param request 登录请求
     * @return 登录响应（含 token 与过期时间）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 刷新 Access Token。
     *
     * @param request 刷新 Token 请求
     * @return 新的 Access Token 与过期时间
     */
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    /**
     * 获取当前登录用户信息。
     *
     * @return 当前用户信息
     */
    UserDTO me();
}
