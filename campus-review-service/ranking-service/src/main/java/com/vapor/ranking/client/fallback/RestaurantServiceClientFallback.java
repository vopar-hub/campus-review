package com.vapor.ranking.client.fallback;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.ranking.client.RestaurantServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 餐馆服务 Feign 客户端降级处理。
 */
@Component
public class RestaurantServiceClientFallback implements RestaurantServiceClient {
    private static final Logger log = LoggerFactory.getLogger(RestaurantServiceClientFallback.class);

    @Override
    public ApiResponse<List<RestaurantDTO>> getRestaurants() {
        log.error("获取餐馆列表失败，返回降级数据");
        return ApiResponse.error("服务暂时不可用，请稍后重试");
    }

    @Override
    public ApiResponse<List<RestaurantDTO>> getRestaurantsByIds(List<Long> ids) {
        log.error("批量获取餐馆失败，ids={}", ids);
        return ApiResponse.error("服务暂时不可用，请稍后重试");
    }
}
