package com.vapor.user.service;

import com.vapor.model.user.UserDTO;

import java.util.List;

/**
 * 用户后台管理服务接口。
 *
 * 提供封禁与解封等管理动作。
 */
public interface UserAdminService {

    /**
     * 封禁用户。
     *
     * @param userId 用户 ID
     */
    void ban(Long userId);

    /**
     * 解封用户。
     *
     * @param userId 用户 ID
     */
    void unban(Long userId);

    /**
     * 获取用户列表。
     *
     * @return 用户列表
     */
    List<UserDTO> getUserList();
}
