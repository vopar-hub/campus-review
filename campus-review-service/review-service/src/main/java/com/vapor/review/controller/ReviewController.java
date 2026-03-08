package com.vapor.review.controller;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.review.ReviewCreateRequest;
import com.vapor.model.review.ReviewDTO;
import com.vapor.review.service.ReviewAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评价接口（用户侧）。
 *
 * 提供评价发布、按餐馆查询与查询我的评价等能力。
 */
@RestController
@RequestMapping("/api/reviews")
@Tag(name = "评价管理", description = "评价发布、查询等接口")
public class ReviewController {
    private final ReviewAppService reviewAppService;

    /**
     * 构造控制器。
     *
     * @param reviewAppService 评价应用服务
     */
    public ReviewController(ReviewAppService reviewAppService) {
        this.reviewAppService = reviewAppService;
    }

    /**
     * 发布评价。
     *
     * 需要登录态，评价初始状态为待审核。
     *
     * @param request 创建请求
     * @return 创建后的评价信息
     */
    @PostMapping
    @Operation(summary = "发布评价", description = "发布新的评价（初始状态为待审核）")
    public ApiResponse<ReviewDTO> create(@Valid @RequestBody ReviewCreateRequest request) {
        return ApiResponse.ok(reviewAppService.create(request));
    }

    /**
     * 查询指定餐馆的已通过评价列表。
     *
     * @param restaurantId 餐馆 ID
     * @return 评价列表
     */
    @GetMapping
    @Operation(summary = "查询餐馆评价", description = "查询指定餐馆的已通过评价列表")
    public ApiResponse<List<ReviewDTO>> listByRestaurant(@RequestParam Long restaurantId) {
        return ApiResponse.ok(reviewAppService.listByRestaurant(restaurantId));
    }

    /**
     * 查询当前登录用户发布的评价列表。
     *
     * @return 我的评价列表
     */
    @GetMapping("/me")
    @Operation(summary = "查询我的评价", description = "查询当前登录用户发布的评价列表")
    public ApiResponse<List<ReviewDTO>> myReviews() {
        return ApiResponse.ok(reviewAppService.myReviews());
    }
}
