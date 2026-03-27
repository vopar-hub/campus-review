package com.vapor.admin.client.fallback;

import com.vapor.admin.client.RestaurantServiceClient;
import com.vapor.common.api.ApiResponse;
import com.vapor.model.restaurant.RestaurantCreateRequest;
import com.vapor.model.restaurant.RestaurantDTO;
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
    public ApiResponse<RestaurantDTO> createRestaurant(RestaurantCreateRequest request) {
        log.error("创建餐馆失败，name={}", request.name());
        return ApiResponse.error("服务暂时不可用，请稍后重试");
    }

    @Override
    public ApiResponse<Void> deleteRestaurant(Long restaurantId) {
        log.error("删除餐馆失败，restaurantId={}", restaurantId);
        return ApiResponse.error("服务暂时不可用，请稍后重试");
    }
}
