package com.vapor.ranking.controller;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.ranking.HotRestaurantRankItemDTO;
import com.vapor.ranking.service.HotRestaurantRankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 排行榜查询接口（用户侧）。
 *
 * 对外提供各类排行榜的查询能力。
 */
@RestController
@RequestMapping("/api/rankings")
@Tag(name = "排行榜", description = "热门餐馆排行榜查询接口")
public class RankingController {
    private final HotRestaurantRankingService hotRestaurantRankingService;

    /**
     * 构造控制器。
     *
     * @param hotRestaurantRankingService 热门餐馆排行榜服务
     */
    public RankingController(HotRestaurantRankingService hotRestaurantRankingService) {
        this.hotRestaurantRankingService = hotRestaurantRankingService;
    }

    /**
     * 查询热门餐馆排行榜。
     *
     * @param topN 返回 Top N，最小为 1
     * @return 热门餐馆排行榜条目
     */
    @GetMapping("/hot-restaurants")
    @Operation(summary = "热门餐馆排行榜", description = "查询热门餐馆排行榜 Top N")
    public ApiResponse<List<HotRestaurantRankItemDTO>> hotRestaurants(@RequestParam(defaultValue = "10") int topN) {
        return ApiResponse.ok(hotRestaurantRankingService.top(topN));
    }
}
