package com.vapor.restaurant.ranking.client;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.interaction.InteractionCountDTO;
import com.vapor.model.review.ReviewDTO;
import com.vapor.restaurant.ranking.client.fallback.ReviewServiceClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 评价服务 Feign 客户端（包含互动数据接口）。
 *
 * review-service 同时提供评价和互动数据接口。
 */
@FeignClient(name = "review-service", fallback = ReviewServiceClientFallback.class)
public interface ReviewServiceClient {

    /**
     * 获取指定餐馆的评价列表。
     *
     * @param restaurantId 餐馆 ID
     * @return 评价列表响应
     */
    @GetMapping("/api/reviews")
    ApiResponse<List<ReviewDTO>> getReviews(@RequestParam("restaurantId") Long restaurantId);

    /**
     * 获取指定目标的互动计数。
     *
     * @param targetType 目标类型
     * @param targetId 目标 ID
     * @return 互动计数响应
     */
    @GetMapping("/api/interactions/count")
    ApiResponse<InteractionCountDTO> getInteractionCount(
            @RequestParam("targetType") String targetType,
            @RequestParam("targetId") Long targetId
    );
}
