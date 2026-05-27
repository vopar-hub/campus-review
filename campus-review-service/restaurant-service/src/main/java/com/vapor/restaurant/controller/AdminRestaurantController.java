package com.vapor.restaurant.controller;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.restaurant.RestaurantCreateRequest;
import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.restaurant.service.RestaurantAdminService;
import com.vapor.restaurant.service.RestaurantAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 餐厅后台管理接口（供后台网关转发）。
 *
 * 提供餐厅列表查询、创建、删除等管理动作。
 */
@RestController
@RequestMapping("/api/admin/restaurants")
@Tag(name = "餐厅管理", description = "餐厅列表查询、创建、删除等管理接口")
public class AdminRestaurantController {
    private final RestaurantAdminService restaurantAdminService;
    private final RestaurantAppService restaurantAppService;

    /**
     * 构造控制器。
     *
     * @param restaurantAdminService 餐厅后台管理应用服务
     * @param restaurantAppService 餐厅应用服务
     */
    public AdminRestaurantController(RestaurantAdminService restaurantAdminService, RestaurantAppService restaurantAppService) {
        this.restaurantAdminService = restaurantAdminService;
        this.restaurantAppService = restaurantAppService;
    }

    /**
     * 获取餐厅列表。
     *
     * @return 餐厅列表
     */
    @GetMapping
    @Operation(summary = "获取餐厅列表", description = "获取所有餐厅列表")
    public ApiResponse<List<RestaurantDTO>> getRestaurantList() {
        return ApiResponse.ok(restaurantAdminService.getRestaurantList());
    }

    /**
     * 创建餐厅。
     *
     * @param request 创建请求
     * @return 创建的餐厅 DTO
     */
    @PostMapping
    @Operation(summary = "创建餐厅", description = "添加新的餐厅信息")
    public ApiResponse<RestaurantDTO> createRestaurant(@Valid @RequestBody RestaurantCreateRequest request) {
        return ApiResponse.ok(restaurantAppService.create(request));
    }

    /**
     * 创建餐厅（带图片上传）。
     *
     * @param name 餐厅名称
     * @param campus 校区
     * @param address 地址（可选）
     * @param description 描述（可选）
     * @param coverImage 封面图片文件（可选）
     * @return 创建的餐厅 DTO
     */
    @PostMapping("/with-image")
    @Operation(summary = "创建餐厅（带图片上传）", description = "添加新的餐厅信息并上传封面图片")
    public ApiResponse<RestaurantDTO> createRestaurantWithImage(
            @RequestParam("name") String name,
            @RequestParam("campus") String campus,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage
    ) {
        RestaurantCreateRequest request = new RestaurantCreateRequest(name, campus, address, description, null);
        RestaurantDTO restaurant;
        if (coverImage != null && !coverImage.isEmpty()) {
            restaurant = restaurantAppService.createWithImage(request, coverImage);
        } else {
            restaurant = restaurantAppService.create(request);
        }
        return ApiResponse.ok(restaurant);
    }

    /**
     * 删除指定餐厅。
     *
     * @param id 餐厅 ID
     * @return 空响应体
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除餐厅", description = "删除指定餐厅")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        restaurantAdminService.delete(id);
        return ApiResponse.ok(null);
    }
}
