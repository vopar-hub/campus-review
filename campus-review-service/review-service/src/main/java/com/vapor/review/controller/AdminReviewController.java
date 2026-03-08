package com.vapor.review.controller;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.review.ReviewDTO;
import com.vapor.review.service.ReviewAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评价后台管理接口（供后台网关转发）。
 *
 * 提供待审核列表查询以及通过/驳回等审核动作。
 */
@RestController
@RequestMapping("/api/admin/reviews")
@Tag(name = "评价审核", description = "评价审核管理接口")
public class AdminReviewController {
    private final ReviewAdminService reviewAdminService;

    /**
     * 构造控制器。
     *
     * @param reviewAdminService 评价后台管理应用服务
     */
    public AdminReviewController(ReviewAdminService reviewAdminService) {
        this.reviewAdminService = reviewAdminService;
    }

    /**
     * 查询待审核评价列表。
     *
     * @return 待审核评价列表
     */
    @GetMapping("/pending")
    @Operation(summary = "待审核评价列表", description = "查询所有待审核的评价列表")
    public ApiResponse<List<ReviewDTO>> pending() {
        return ApiResponse.ok(reviewAdminService.pending());
    }

    /**
     * 通过指定评价。
     *
     * @param id 评价 ID
     * @return 空响应体
     */
    @PostMapping("/{id}/approve")
    @Operation(summary = "通过评价", description = "审核通过指定评价")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        reviewAdminService.approve(id);
        return ApiResponse.ok(null);
    }

    /**
     * 驳回指定评价。
     *
     * @param id 评价 ID
     * @return 空响应体
     */
    @PostMapping("/{id}/reject")
    @Operation(summary = "驳回评价", description = "审核驳回指定评价")
    public ApiResponse<Void> reject(@PathVariable Long id) {
        reviewAdminService.reject(id);
        return ApiResponse.ok(null);
    }
}
