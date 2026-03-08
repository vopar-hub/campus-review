package com.vapor.user.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.vapor.common.error.BizException;
import com.vapor.common.error.ErrorCode;
import com.vapor.common.util.UserContextUtil;
import com.vapor.user.entity.UserEntity;
import com.vapor.user.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 用户后台管理应用服务。
 *
 * 提供封禁与解封等管理动作，并在服务内做最小权限校验。
 */
@Service
public class UserAdminService {
    private static final Logger log = LoggerFactory.getLogger(UserAdminService.class);

    private final UserMapper userMapper;

    /**
     * 构造应用服务。
     *
     * @param userMapper 用户数据访问组件
     */
    public UserAdminService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 封禁用户。
     *
     * @param userId 用户 ID
     * @throws BizException 非管理员或用户不存在时抛出
     */
    @Transactional
    public void ban(Long userId) {
        UserContextUtil.requireAdmin();
        Long adminId = UserContextUtil.requireUserId();
        log.info("封禁用户：userId={}, adminId={}", userId, adminId);

        int updated = userMapper.update(new LambdaUpdateWrapper<UserEntity>()
                .eq(UserEntity::getId, userId)
                .set(UserEntity::getBanned, true)
                .set(UserEntity::getUpdatedAt, Instant.now()));
        if (updated == 0) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
    }

    /**
     * 解封用户。
     *
     * @param userId 用户 ID
     * @throws BizException 非管理员或用户不存在时抛出
     */
    @Transactional
    public void unban(Long userId) {
        UserContextUtil.requireAdmin();
        Long adminId = UserContextUtil.requireUserId();
        log.info("解封用户：userId={}, adminId={}", userId, adminId);

        int updated = userMapper.update(new LambdaUpdateWrapper<UserEntity>()
                .eq(UserEntity::getId, userId)
                .set(UserEntity::getBanned, false)
                .set(UserEntity::getUpdatedAt, Instant.now()));
        if (updated == 0) {
            throw new BizException(ErrorCode.NOT_FOUND, "用户不存在");
        }
    }
}
