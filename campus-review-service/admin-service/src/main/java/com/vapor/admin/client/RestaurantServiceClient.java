package com.vapor.admin.client;

import com.vapor.admin.client.fallback.RestaurantServiceClientFallback;
import com.vapor.common.api.ApiResponse;
import com.vapor.model.restaurant.RestaurantCreateRequest;
import com.vapor.model.restaurant.RestaurantDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 餐馆服务 Feign 客户端（管理端）。
 */
@FeignClient(name = "restaurant-service", fallback = RestaurantServiceClientFallback.class)
public interface RestaurantServiceClient {

    /**
     * 获取餐馆列表。
     *
     * @return 餐馆列表响应
     */
    @GetMapping("/api/admin/restaurants")
    ApiResponse<List<RestaurantDTO>> getRestaurants();

    /**
     * 创建餐馆。
     *
     * @param request 创建请求
     * @return 创建的餐馆 DTO 响应
     */
    @PostMapping("/api/admin/restaurants")
    ApiResponse<RestaurantDTO> createRestaurant(@RequestBody RestaurantCreateRequest request);

    /**
     * 删除指定餐馆。
     *
     * @param restaurantId 餐馆 ID
     * @return 空响应
     */
    @DeleteMapping("/api/admin/restaurants/{restaurantId}")
    ApiResponse<Void> deleteRestaurant(@PathVariable("restaurantId") Long restaurantId);
}
