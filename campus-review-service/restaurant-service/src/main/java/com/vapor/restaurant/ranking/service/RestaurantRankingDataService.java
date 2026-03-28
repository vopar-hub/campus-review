package com.vapor.restaurant.ranking.service;

import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.restaurant.service.RestaurantAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 餐馆排行榜数据服务。
 * <p>
 * 为排行榜提供餐馆数据查询能力，避免使用 FeignClient 进行本地循环调用。
 */
@Service
public class RestaurantRankingDataService {
    private static final Logger log = LoggerFactory.getLogger(RestaurantRankingDataService.class);

    private final RestaurantAppService restaurantAppService;

    /**
     * 构造服务。
     *
     * @param restaurantAppService 餐馆应用服务
     */
    public RestaurantRankingDataService(RestaurantAppService restaurantAppService) {
        this.restaurantAppService = restaurantAppService;
    }

    /**
     * 获取所有餐馆列表。
     *
     * @return 餐馆列表
     */
    public List<RestaurantDTO> getAllRestaurants() {
        log.debug("获取所有餐馆列表");
        return restaurantAppService.search(null, null);
    }

    /**
     * 根据 ID 列表批量获取餐馆信息。
     *
     * @param ids 餐馆 ID 列表
     * @return 餐馆列表
     */
    public List<RestaurantDTO> getRestaurantsByIds(List<Long> ids) {
        log.debug("批量获取餐馆：ids={}", ids);
        return restaurantAppService.getByIds(ids);
    }
}
