package com.vapor.admin.service;

import com.vapor.common.api.ApiResponse;
import com.vapor.common.error.BizException;
import com.vapor.common.util.UserContextUtil;
import com.vapor.common.web.UserContext;
import com.vapor.model.restaurant.RestaurantCreateRequest;
import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.model.user.UserDTO;
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
 * 统一在后台服务中编排调用下游微服务（用户/餐厅等），并透传管理员身份信息。
 */
@Service
public class AdminOrchestratorService {
    private static final Logger log = LoggerFactory.getLogger(AdminOrchestratorService.class);
    private static final ParameterizedTypeReference<ApiResponse<List<UserDTO>>> USER_LIST_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<ApiResponse<List<RestaurantDTO>>> RESTAURANT_LIST_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<ApiResponse<RestaurantDTO>> RESTAURANT_CREATE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;
    private final String userServiceBaseUrl;
    private final String restaurantServiceBaseUrl;

    /**
     * 构造编排服务。
     *
     * @param restClient HTTP 客户端
     * @param userServiceBaseUrl 用户服务基础地址
     * @param restaurantServiceBaseUrl 餐厅服务基础地址
     */
    public AdminOrchestratorService(
            RestClient restClient,
            @Value("${downstream.user-service-base-url}") String userServiceBaseUrl,
            @Value("${downstream.restaurant-service-base-url}") String restaurantServiceBaseUrl
    ) {
        this.restClient = restClient;
        this.userServiceBaseUrl = userServiceBaseUrl;
        this.restaurantServiceBaseUrl = restaurantServiceBaseUrl;
    }

    /**
     * 获取用户列表。
     *
     * @return 用户列表
     * @throws BizException 非管理员时抛出
     */
    public List<UserDTO> getUserList() {
        UserContext ctx = UserContextUtil.requireUserContext();
        log.info("获取用户列表：adminId={}", ctx.getUserId());

        ApiResponse<List<UserDTO>> resp = restClient.get()
                .uri(userServiceBaseUrl + "/api/admin/users")
                .header("X-User-Id", String.valueOf(ctx.getUserId()))
                .header("X-User-Roles", String.join(",", ctx.getRoles()))
                .retrieve()
                .body(USER_LIST_TYPE);
        return resp == null || resp.getData() == null ? List.of() : resp.getData();
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

    /**
     * 获取餐厅列表。
     *
     * @return 餐厅列表
     * @throws BizException 非管理员时抛出
     */
    public List<RestaurantDTO> getRestaurantList() {
        UserContext ctx = UserContextUtil.requireUserContext();
        log.info("获取餐厅列表：adminId={}", ctx.getUserId());

        ApiResponse<List<RestaurantDTO>> resp = restClient.get()
                .uri(restaurantServiceBaseUrl + "/api/admin/restaurants")
                .header("X-User-Id", String.valueOf(ctx.getUserId()))
                .header("X-User-Roles", String.join(",", ctx.getRoles()))
                .retrieve()
                .body(RESTAURANT_LIST_TYPE);
        return resp == null || resp.getData() == null ? List.of() : resp.getData();
    }

    /**
     * 创建餐厅。
     *
     * @param request 创建请求
     * @return 创建的餐厅 DTO
     * @throws BizException 非管理员时抛出
     */
    public RestaurantDTO createRestaurant(RestaurantCreateRequest request) {
        UserContext ctx = UserContextUtil.requireUserContext();
        log.info("创建餐厅：name={}, adminId={}", request.name(), ctx.getUserId());

        ApiResponse<RestaurantDTO> resp = restClient.post()
                .uri(restaurantServiceBaseUrl + "/api/admin/restaurants")
                .header("X-User-Id", String.valueOf(ctx.getUserId()))
                .header("X-User-Roles", String.join(",", ctx.getRoles()))
                .body(request)
                .retrieve()
                .body(RESTAURANT_CREATE_TYPE);
        return resp != null ? resp.getData() : null;
    }

    /**
     * 删除指定餐厅。
     *
     * @param restaurantId 餐厅 ID
     * @throws BizException 非管理员时抛出
     */
    public void deleteRestaurant(Long restaurantId) {
        UserContext ctx = UserContextUtil.requireUserContext();
        log.info("删除餐厅：restaurantId={}, adminId={}", restaurantId, ctx.getUserId());

        restClient.delete()
                .uri(restaurantServiceBaseUrl + "/api/admin/restaurants/" + restaurantId)
                .header("X-User-Id", String.valueOf(ctx.getUserId()))
                .header("X-User-Roles", String.join(",", ctx.getRoles()))
                .retrieve()
                .toBodilessEntity();
    }
}
