package com.vapor.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * 用户表实体。
 *
 * 对应数据库 users 表，包含认证与权限相关字段（如密码散列、角色、封禁状态等）。
 */
@Data
@TableName("users")
public class UserEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String email;
    private String studentNo;
    private String passwordHash;
    private String nickname;
    private String roles;
    private Boolean banned;
    private Instant createdAt;
    private Instant updatedAt;
}
