package com.vapor.restaurant.controller;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.restaurant.RestaurantCreateRequest;
import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.restaurant.service.RestaurantAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 餐馆接口（用户侧）。
 *
 * 提供餐馆创建、按 ID 查询与检索等能力。
 */
@RestController
@RequestMapping("/api/restaurants")
@Tag(name = "餐馆管理", description = "餐馆创建、查询与检索接口")
public class RestaurantController {
    private final RestaurantAppService restaurantAppService;

    /**
     * 构造控制器。
     *
     * @param restaurantAppService 餐馆应用服务
     */
    public RestaurantController(RestaurantAppService restaurantAppService) {
        this.restaurantAppService = restaurantAppService;
    }

    /**
     * 创建餐馆。
     *
     * @param request 创建请求
     * @return 创建后的餐馆信息
     */
    @PostMapping
    @Operation(summary = "创建餐馆", description = "创建新的餐馆记录")
    public ApiResponse<RestaurantDTO> create(@Valid @RequestBody RestaurantCreateRequest request) {
        return ApiResponse.ok(restaurantAppService.create(request));
    }

    /**
     * 按 ID 查询餐馆。
     *
     * @param id 餐馆 ID
     * @return 餐馆信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询餐馆", description = "根据 ID 查询餐馆详细信息")
    public ApiResponse<RestaurantDTO> getById(@PathVariable Long id) {
        return ApiResponse.ok(restaurantAppService.getById(id));
    }

    /**
     * 餐馆检索。
     *
     * @param name 餐馆名称（模糊匹配，可选）
     * @param campus 校区（精确匹配，可选）
     * @return 结果列表（按 ID 倒序）
     */
    @GetMapping
    @Operation(summary = "搜索餐馆", description = "根据名称和校区搜索餐馆")
    public ApiResponse<List<RestaurantDTO>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String campus
    ) {
        return ApiResponse.ok(restaurantAppService.search(name, campus));
    }
}
