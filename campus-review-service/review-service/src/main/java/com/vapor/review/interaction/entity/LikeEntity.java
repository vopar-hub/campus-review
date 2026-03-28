package com.vapor.review.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * 点赞表实体。
 *
 * 对应数据库 likes 表，记录用户对目标对象的点赞关系。
 */
@Data
@TableName("likes")
public class LikeEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String targetType;
    private Long targetId;
    private Instant createdAt;
}
