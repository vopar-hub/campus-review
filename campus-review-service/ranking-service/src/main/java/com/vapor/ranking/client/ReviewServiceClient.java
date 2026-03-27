package com.vapor.ranking.client;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.review.ReviewDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 评价服务 Feign 客户端。
 */
@FeignClient(name = "review-service")
public interface ReviewServiceClient {

    /**
     * 获取指定餐馆的评价列表。
     *
     * @param restaurantId 餐馆 ID
     * @return 评价列表响应
     */
    @GetMapping("/api/reviews")
    ApiResponse<List<ReviewDTO>> getReviews(@RequestParam("restaurantId") Long restaurantId);
}
