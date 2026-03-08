package com.vapor.model.ranking;

/**
 * 热门餐馆榜单条目 DTO。
 *
 * @param rank 名次
 * @param restaurantId 餐馆 ID
 * @param restaurantName 餐馆名称
 * @param score 热度分值
 */
public record HotRestaurantRankItemDTO(
        long rank,
        Long restaurantId,
        String restaurantName,
        double score
) {
}
