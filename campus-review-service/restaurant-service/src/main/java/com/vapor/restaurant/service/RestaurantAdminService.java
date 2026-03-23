package com.vapor.restaurant.service;

import com.vapor.model.restaurant.RestaurantDTO;

import java.util.List;

/**
 * 餐厅后台管理服务接口。
 *
 * 提供餐厅列表查询、删除等管理动作。
 */
public interface RestaurantAdminService {

    /**
     * 获取餐厅列表。
     *
     * @return 餐厅列表
     */
    List<RestaurantDTO> getRestaurantList();

    /**
     * 删除指定餐厅。
     *
     * @param restaurantId 餐厅 ID
     */
    void delete(Long restaurantId);
}
