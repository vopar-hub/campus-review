package com.vapor.model.interaction;

/**
 * 互动统计 DTO。
 *
 * @param targetType 目标类型（如 RESTAURANT、REVIEW）
 * @param targetId 目标 ID
 * @param likeCount 点赞数
 * @param favoriteCount 收藏数
 */
public record InteractionCountDTO(
        String targetType,
        Long targetId,
        long likeCount,
        long favoriteCount
) {
}
