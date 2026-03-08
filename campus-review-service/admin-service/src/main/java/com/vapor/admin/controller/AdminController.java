package com.vapor.admin.controller;

import com.vapor.admin.service.AdminOrchestratorService;
import com.vapor.common.api.ApiResponse;
import com.vapor.model.review.ReviewDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台管理接口（供后台网关转发）。
 *
 * 统一提供后台审核与用户封禁等入口，具体业务由编排服务转发到下游微服务执行。
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "后台管理", description = "后台审核与用户管理接口")
public class AdminController {
    private final AdminOrchestratorService adminOrchestratorService;

    /**
     * 构造控制器。
     *
     * @param adminOrchestratorService 后台编排服务
     */
    public AdminController(AdminOrchestratorService adminOrchestratorService) {
        this.adminOrchestratorService = adminOrchestratorService;
    }

    /**
     * 查询待审核评价列表。
     *
     * @return 待审核评价列表
     */
    @GetMapping("/reviews/pending")
    @Operation(summary = "待审核评价列表", description = "查询所有待审核的评价列表")
    public ApiResponse<List<ReviewDTO>> pendingReviews() {
        return ApiResponse.ok(adminOrchestratorService.pendingReviews());
    }

    /**
     * 通过指定评价。
     *
     * @param id 评价 ID
     * @return 空响应体
     */
    @PostMapping("/reviews/{id}/approve")
    @Operation(summary = "通过评价", description = "审核通过指定评价")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        adminOrchestratorService.approveReview(id);
        return ApiResponse.ok(null);
    }

    /**
     * 驳回指定评价。
     *
     * @param id 评价 ID
     * @return 空响应体
     */
    @PostMapping("/reviews/{id}/reject")
    @Operation(summary = "驳回评价", description = "审核驳回指定评价")
    public ApiResponse<Void> reject(@PathVariable Long id) {
        adminOrchestratorService.rejectReview(id);
        return ApiResponse.ok(null);
    }

    /**
     * 封禁指定用户。
     *
     * @param id 用户 ID
     * @return 空响应体
     */
    @PostMapping("/users/{id}/ban")
    @Operation(summary = "封禁用户", description = "封禁指定用户账号")
    public ApiResponse<Void> banUser(@PathVariable Long id) {
        adminOrchestratorService.banUser(id);
        return ApiResponse.ok(null);
    }

    /**
     * 解封指定用户。
     *
     * @param id 用户 ID
     * @return 空响应体
     */
    @PostMapping("/users/{id}/unban")
    @Operation(summary = "解封用户", description = "解封指定用户账号")
    public ApiResponse<Void> unbanUser(@PathVariable Long id) {
        adminOrchestratorService.unbanUser(id);
        return ApiResponse.ok(null);
    }
}
