package com.vapor.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.vapor.common.error.BizException;
import com.vapor.common.error.ErrorCode;
import com.vapor.common.util.UserContextUtil;
import com.vapor.model.user.UserDTO;
import com.vapor.user.entity.UserEntity;
import com.vapor.user.mapper.UserMapper;
import com.vapor.user.service.UserAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * 用户后台管理服务实现类。
 */
@Service
public class UserAdminServiceImpl implements UserAdminService {
    private static final Logger log = LoggerFactory.getLogger(UserAdminServiceImpl.class);

    private final UserMapper userMapper;

    /**
     * 构造应用服务。
     *
     * @param userMapper 用户数据访问组件
     */
    public UserAdminServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
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

    @Override
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

    @Override
    public List<UserDTO> getUserList() {
        UserContextUtil.requireAdmin();
        Long adminId = UserContextUtil.requireUserId();
        log.info("获取用户列表：adminId={}", adminId);

        List<UserEntity> users = userMapper.selectList(null);
        return users.stream().map(this::toDTO).toList();
    }

    /**
     * 将实体转换为 DTO。
     *
     * @param entity 用户实体
     * @return 用户 DTO
     */
    private UserDTO toDTO(UserEntity entity) {
        return new UserDTO(
                entity.getId(),
                entity.getEmail(),
                entity.getStudentNo(),
                entity.getNickname(),
                entity.getRoles() != null ? Set.of(entity.getRoles().split(",")) : Set.of(),
                entity.getBanned() != null && entity.getBanned(),
                entity.getCreatedAt()
        );
    }
}
