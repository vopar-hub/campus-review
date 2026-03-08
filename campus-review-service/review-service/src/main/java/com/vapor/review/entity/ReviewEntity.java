package com.vapor.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * 评价表实体。
 *
 * 对应数据库 reviews 表，包含评分、内容与审核状态等字段。
 */
@Data
@TableName("reviews")
public class ReviewEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long restaurantId;
    private Long userId;
    private Integer rating;
    private String content;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
