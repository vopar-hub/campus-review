package com.vapor.admin.service;

import com.vapor.admin.client.RestaurantServiceClient;
import com.vapor.admin.client.UserServiceClient;
import com.vapor.common.error.BizException;
import com.vapor.common.util.UserContextUtil;
import com.vapor.common.web.UserContext;
import com.vapor.model.restaurant.RestaurantCreateRequest;
import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.model.user.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台编排服务。
 *
 * 统一在后台服务中编排调用下游微服务（用户/餐厅等），并透传管理员身份信息。
 */
@Service
public class AdminOrchestratorService {
    private static final Logger log = LoggerFactory.getLogger(AdminOrchestratorService.class);

    private final UserServiceClient userServiceClient;
    private final RestaurantServiceClient restaurantServiceClient;

    /**
     * 构造编排服务。
     *
     * @param userServiceClient 用户服务 Feign 客户端
     * @param restaurantServiceClient 餐馆服务 Feign 客户端
     */
    public AdminOrchestratorService(
            UserServiceClient userServiceClient,
            RestaurantServiceClient restaurantServiceClient
    ) {
        this.userServiceClient = userServiceClient;
        this.restaurantServiceClient = restaurantServiceClient;
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

        ApiResponse<List<UserDTO>> resp = userServiceClient.getUsers();
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

        userServiceClient.banUser(userId);
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

        userServiceClient.unbanUser(userId);
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

        ApiResponse<List<RestaurantDTO>> resp = restaurantServiceClient.getRestaurants();
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

        ApiResponse<RestaurantDTO> resp = restaurantServiceClient.createRestaurant(request);
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

        restaurantServiceClient.deleteRestaurant(restaurantId);
    }
}
