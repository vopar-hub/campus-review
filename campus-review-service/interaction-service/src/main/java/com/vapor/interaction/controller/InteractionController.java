package com.vapor.interaction.controller;

import com.vapor.common.api.ApiResponse;
import com.vapor.interaction.service.InteractionAppService;
import com.vapor.model.interaction.InteractRequest;
import com.vapor.model.interaction.InteractionCountDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 互动接口（用户侧）。
 *
 * 提供点赞/取消点赞、收藏/取消收藏以及互动计数查询等能力。
 */
@RestController
@RequestMapping("/api/interactions")
@Tag(name = "互动管理", description = "点赞、收藏等互动操作接口")
public class InteractionController {
    private final InteractionAppService interactionAppService;

    /**
     * 构造控制器。
     *
     * @param interactionAppService 互动应用服务
     */
    public InteractionController(InteractionAppService interactionAppService) {
        this.interactionAppService = interactionAppService;
    }

    /**
     * 点赞目标对象。
     *
     * @param request 互动请求（目标类型与 ID）
     * @return 空响应体
     */
    @PostMapping("/like")
    @Operation(summary = "点赞", description = "点赞目标对象（幂等操作）")
    public ApiResponse<Void> like(@Valid @RequestBody InteractRequest request) {
        interactionAppService.like(request);
        return ApiResponse.ok(null);
    }

    /**
     * 取消点赞目标对象。
     *
     * @param request 互动请求（目标类型与 ID）
     * @return 空响应体
     */
    @PostMapping("/unlike")
    @Operation(summary = "取消点赞", description = "取消对目标对象的点赞（幂等操作）")
    public ApiResponse<Void> unlike(@Valid @RequestBody InteractRequest request) {
        interactionAppService.unlike(request);
        return ApiResponse.ok(null);
    }

    /**
     * 收藏目标对象。
     *
     * @param request 互动请求（目标类型与 ID）
     * @return 空响应体
     */
    @PostMapping("/favorite")
    @Operation(summary = "收藏", description = "收藏目标对象（幂等操作）")
    public ApiResponse<Void> favorite(@Valid @RequestBody InteractRequest request) {
        interactionAppService.favorite(request);
        return ApiResponse.ok(null);
    }

    /**
     * 取消收藏目标对象。
     *
     * @param request 互动请求（目标类型与 ID）
     * @return 空响应体
     */
    @PostMapping("/unfavorite")
    @Operation(summary = "取消收藏", description = "取消对目标对象的收藏（幂等操作）")
    public ApiResponse<Void> unfavorite(@Valid @RequestBody InteractRequest request) {
        interactionAppService.unfavorite(request);
        return ApiResponse.ok(null);
    }

    /**
     * 查询目标对象的互动计数。
     *
     * @param targetType 目标类型（如 restaurant/review）
     * @param targetId 目标 ID
     * @return 互动计数
     */
    @GetMapping("/count")
    @Operation(summary = "查询互动计数", description = "查询目标对象的点赞数和收藏数")
    public ApiResponse<InteractionCountDTO> count(@RequestParam String targetType, @RequestParam Long targetId) {
        return ApiResponse.ok(interactionAppService.count(targetType, targetId));
    }
}
