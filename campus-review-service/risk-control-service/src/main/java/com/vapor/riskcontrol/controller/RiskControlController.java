package com.vapor.riskcontrol.controller;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.risk.RateLimitResult;
import com.vapor.model.risk.RiskAuditRequest;
import com.vapor.model.risk.RiskAuditResult;
import com.vapor.riskcontrol.service.RiskControlAppService;
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
 * 风控接口（对内服务）。
 *
 * 提供内容审核与限流判定能力，供网关或业务服务调用。
 */
@RestController
@RequestMapping("/api/risk")
@Tag(name = "风控管理", description = "内容审核与限流接口")
public class RiskControlController {
    private final RiskControlAppService riskControlAppService;

    /**
     * 构造控制器。
     *
     * @param riskControlAppService 风控应用服务
     */
    public RiskControlController(RiskControlAppService riskControlAppService) {
        this.riskControlAppService = riskControlAppService;
    }

    /**
     * 审核文本内容是否可发布。
     *
     * @param request 审核请求
     * @return 审核结果（是否通过及原因）
     */
    @PostMapping("/audit")
    @Operation(summary = "内容审核", description = "审核文本内容是否包含敏感词")
    public ApiResponse<RiskAuditResult> audit(@Valid @RequestBody RiskAuditRequest request) {
        return ApiResponse.ok(riskControlAppService.auditContent(request.content()));
    }

    /**
     * 进行固定窗口限流判定。
     *
     * @param key 限流键（可为用户 ID、IP 等）
     * @param limit 窗口内允许的最大请求数
     * @param windowSeconds 窗口大小（秒）
     * @return 限流结果（是否允许、剩余次数、重置时间）
     */
    @GetMapping("/ratelimit")
    @Operation(summary = "限流判定", description = "固定窗口限流判定")
    public ApiResponse<RateLimitResult> rateLimit(
            @RequestParam String key,
            @RequestParam(defaultValue = "60") long limit,
            @RequestParam(defaultValue = "60") long windowSeconds
    ) {
        return ApiResponse.ok(riskControlAppService.rateLimit(key, limit, windowSeconds));
    }
}
