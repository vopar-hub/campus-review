package com.vapor.restaurant.ranking.client;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.restaurant.ranking.client.fallback.RestaurantServiceClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 餐馆服务 Feign 客户端（本地调用）。
 */
@FeignClient(name = "restaurant-service", fallback = RestaurantServiceClientFallback.class)
public interface RestaurantServiceClient {

    /**
     * 获取所有餐馆列表。
     *
     * @return 餐馆列表响应
     */
    @GetMapping("/api/restaurants")
    ApiResponse<List<RestaurantDTO>> getRestaurants();

    /**
     * 根据 ID 列表批量获取餐馆信息。
     *
     * @param ids 餐馆 ID 列表
     * @return 餐馆列表响应
     */
    @GetMapping("/api/restaurants/by-ids")
    ApiResponse<List<RestaurantDTO>> getRestaurantsByIds(@RequestParam("ids") List<Long> ids);
}
