package com.vapor.admin.client;

import com.vapor.admin.client.fallback.RestaurantServiceClientFallback;
import com.vapor.common.api.ApiResponse;
import com.vapor.model.restaurant.RestaurantCreateRequest;
import com.vapor.model.restaurant.RestaurantDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 餐馆服务 Feign 客户端降级测试。
 */
class RestaurantServiceClientFallbackTest {

    @InjectMocks
    private RestaurantServiceClientFallback fallback;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRestaurants_Fallback() {
        // 执行
        ApiResponse<List<RestaurantDTO>> result = fallback.getRestaurants();

        // 验证
        assertNotNull(result);
        assertFalse(result.getSuccess());
        assertEquals("服务暂时不可用，请稍后重试", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testCreateRestaurant_Fallback() {
        // 准备
        RestaurantCreateRequest request = new RestaurantCreateRequest("Test Restaurant", "Address", "123456");

        // 执行
        ApiResponse<RestaurantDTO> result = fallback.createRestaurant(request);

        // 验证
        assertNotNull(result);
        assertFalse(result.getSuccess());
        assertEquals("服务暂时不可用，请稍后重试", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testDeleteRestaurant_Fallback() {
        // 执行
        ApiResponse<Void> result = fallback.deleteRestaurant(1L);

        // 验证
        assertNotNull(result);
        assertFalse(result.getSuccess());
        assertEquals("服务暂时不可用，请稍后重试", result.getMessage());
    }
}
