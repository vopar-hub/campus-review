package com.vapor.admin.client.fallback;

import com.vapor.admin.client.UserServiceClient;
import com.vapor.common.api.ApiResponse;
import com.vapor.common.error.ErrorCode;
import com.vapor.model.user.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户服务 Feign 客户端降级处理。
 */
@Component
public class UserServiceClientFallback implements UserServiceClient {
    private static final Logger log = LoggerFactory.getLogger(UserServiceClientFallback.class);

    @Override
    public ApiResponse<List<UserDTO>> getUsers() {
        log.error("获取用户列表失败，返回降级数据");
        return ApiResponse.fail(ErrorCode.INTERNAL_ERROR, "服务暂时不可用，请稍后重试", null);
    }

    @Override
    public ApiResponse<Void> banUser(Long userId) {
        log.error("封禁用户失败，userId={}", userId);
        return ApiResponse.fail(ErrorCode.INTERNAL_ERROR, "服务暂时不可用，请稍后重试", null);
    }

    @Override
    public ApiResponse<Void> unbanUser(Long userId) {
        log.error("解封用户失败，userId={}", userId);
        return ApiResponse.fail(ErrorCode.INTERNAL_ERROR, "服务暂时不可用，请稍后重试", null);
    }
}
