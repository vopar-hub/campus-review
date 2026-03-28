package com.vapor.review.interaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * 收藏表实体。
 *
 * 对应数据库 favorites 表，记录用户对目标对象的收藏关系。
 */
@Data
@TableName("favorites")
public class FavoriteEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String targetType;
    private Long targetId;
    private Instant createdAt;
}
