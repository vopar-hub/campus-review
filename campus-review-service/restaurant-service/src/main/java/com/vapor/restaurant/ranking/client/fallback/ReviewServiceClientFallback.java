package com.vapor.restaurant.ranking.client.fallback;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.review.ReviewDTO;
import com.vapor.restaurant.ranking.client.ReviewServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 评价服务 Feign 客户端降级处理。
 */
@Component
public class ReviewServiceClientFallback implements ReviewServiceClient {
    private static final Logger log = LoggerFactory.getLogger(ReviewServiceClientFallback.class);

    @Override
    public ApiResponse<List<ReviewDTO>> getReviews(Long restaurantId) {
        log.error("获取评价列表失败，restaurantId={}", restaurantId);
        return ApiResponse.ok(List.of());
    }
}
