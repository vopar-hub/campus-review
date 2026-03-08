package com.vapor.admin.service;

import com.vapor.common.api.ApiResponse;
import com.vapor.common.error.BizException;
import com.vapor.common.util.UserContextUtil;
import com.vapor.common.web.UserContext;
import com.vapor.model.review.ReviewDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * 后台编排服务。
 *
 * 统一在后台服务中编排调用下游微服务（用户/评价等），并透传管理员身份信息。
 */
@Service
public class AdminOrchestratorService {
    private static final Logger log = LoggerFactory.getLogger(AdminOrchestratorService.class);
    private static final ParameterizedTypeReference<ApiResponse<List<ReviewDTO>>> REVIEW_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;
    private final String userServiceBaseUrl;
    private final String reviewServiceBaseUrl;

    /**
     * 构造编排服务。
     *
     * @param restClient HTTP 客户端
     * @param userServiceBaseUrl 用户服务基础地址
     * @param reviewServiceBaseUrl 评价服务基础地址
     */
    public AdminOrchestratorService(
            RestClient restClient,
            @Value("${downstream.user-service-base-url}") String userServiceBaseUrl,
            @Value("${downstream.review-service-base-url}") String reviewServiceBaseUrl
    ) {
        this.restClient = restClient;
        this.userServiceBaseUrl = userServiceBaseUrl;
        this.reviewServiceBaseUrl = reviewServiceBaseUrl;
    }

    /**
     * 查询待审核评价列表。
     *
     * @return 待审核评价列表
     * @throws BizException 非管理员时抛出
     */
    public List<ReviewDTO> pendingReviews() {
        UserContext ctx = UserContextUtil.requireUserContext();
        log.info("查询待审核评价列表：adminId={}", ctx.getUserId());

        ApiResponse<List<ReviewDTO>> resp = restClient.get()
                .uri(reviewServiceBaseUrl + "/api/admin/reviews/pending")
                .header("X-User-Id", String.valueOf(ctx.getUserId()))
                .header("X-User-Roles", String.join(",", ctx.getRoles()))
                .retrieve()
                .body(REVIEW_LIST_TYPE);
        return resp == null || resp.getData() == null ? List.of() : resp.getData();
    }

    /**
     * 通过指定评价。
     *
     * @param reviewId 评价 ID
     * @throws BizException 非管理员时抛出
     */
    public void approveReview(Long reviewId) {
        UserContext ctx = UserContextUtil.requireUserContext();
        log.info("通过评价：reviewId={}, adminId={}", reviewId, ctx.getUserId());

        restClient.post()
                .uri(reviewServiceBaseUrl + "/api/admin/reviews/" + reviewId + "/approve")
                .header("X-User-Id", String.valueOf(ctx.getUserId()))
                .header("X-User-Roles", String.join(",", ctx.getRoles()))
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * 驳回指定评价。
     *
     * @param reviewId 评价 ID
     * @throws BizException 非管理员时抛出
     */
    public void rejectReview(Long reviewId) {
        UserContext ctx = UserContextUtil.requireUserContext();
        log.info("驳回评价：reviewId={}, adminId={}", reviewId, ctx.getUserId());

        restClient.post()
                .uri(reviewServiceBaseUrl + "/api/admin/reviews/" + reviewId + "/reject")
                .header("X-User-Id", String.valueOf(ctx.getUserId()))
                .header("X-User-Roles", String.join(",", ctx.getRoles()))
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * 封禁指定用户。
     *
     * @param userId 用户 ID
     * @throws BizException 非管理员时抛出
     */
    public void banUser(Long userId) {
        UserContext ctx = UserContextUtil.requireUserContext();
        log.info("封禁用户：userId={}, adminId={}", userId, ctx.getUserId());

        restClient.post()
                .uri(userServiceBaseUrl + "/api/admin/users/" + userId + "/ban")
                .header("X-User-Id", String.valueOf(ctx.getUserId()))
                .header("X-User-Roles", String.join(",", ctx.getRoles()))
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * 解封指定用户。
     *
     * @param userId 用户 ID
     * @throws BizException 非管理员时抛出
     */
    public void unbanUser(Long userId) {
        UserContext ctx = UserContextUtil.requireUserContext();
        log.info("解封用户：userId={}, adminId={}", userId, ctx.getUserId());

        restClient.post()
                .uri(userServiceBaseUrl + "/api/admin/users/" + userId + "/unban")
                .header("X-User-Id", String.valueOf(ctx.getUserId()))
                .header("X-User-Roles", String.join(",", ctx.getRoles()))
                .retrieve()
                .toBodilessEntity();
    }
}
