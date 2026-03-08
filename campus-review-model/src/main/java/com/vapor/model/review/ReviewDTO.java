package com.vapor.model.review;

import java.time.Instant;

/**
 * 评价 DTO。
 *
 * @param id 评价 ID
 * @param restaurantId 餐馆 ID
 * @param userId 发布用户 ID
 * @param rating 评分（1~5）
 * @param content 评价内容
 * @param status 状态（如 PENDING、APPROVED、REJECTED）
 * @param createdAt 创建时间
 */
public record ReviewDTO(
        Long id,
        Long restaurantId,
        Long userId,
        int rating,
        String content,
        String status,
        Instant createdAt
) {
}
