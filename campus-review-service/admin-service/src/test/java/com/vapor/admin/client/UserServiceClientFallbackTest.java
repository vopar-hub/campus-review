package com.vapor.admin.client;

import com.vapor.admin.client.fallback.UserServiceClientFallback;
import com.vapor.common.api.ApiResponse;
import com.vapor.model.user.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 用户服务 Feign 客户端降级测试。
 */
class UserServiceClientFallbackTest {

    @InjectMocks
    private UserServiceClientFallback fallback;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUsers_Fallback() {
        // 执行
        ApiResponse<List<UserDTO>> result = fallback.getUsers();

        // 验证
        assertNotNull(result);
        assertFalse(result.getSuccess());
        assertEquals("服务暂时不可用，请稍后重试", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testBanUser_Fallback() {
        // 执行
        ApiResponse<Void> result = fallback.banUser(1L);

        // 验证
        assertNotNull(result);
        assertFalse(result.getSuccess());
        assertEquals("服务暂时不可用，请稍后重试", result.getMessage());
    }

    @Test
    void testUnbanUser_Fallback() {
        // 执行
        ApiResponse<Void> result = fallback.unbanUser(1L);

        // 验证
        assertNotNull(result);
        assertFalse(result.getSuccess());
        assertEquals("服务暂时不可用，请稍后重试", result.getMessage());
    }
}
