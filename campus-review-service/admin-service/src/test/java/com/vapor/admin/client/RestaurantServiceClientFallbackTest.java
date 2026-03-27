package com.vapor.admin.client;

import com.vapor.admin.client.fallback.RestaurantServiceClientFallback;
import com.vapor.common.api.ApiResponse;
import com.vapor.common.error.ErrorCode;
import com.vapor.model.restaurant.RestaurantCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

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
        var result = fallback.getRestaurants();

        // 验证
        assertNotNull(result);
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), result.getCode());
        assertEquals("服务暂时不可用，请稍后重试", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testCreateRestaurant_Fallback() {
        // 准备
        var request = new RestaurantCreateRequest("Test Restaurant", "Address", "123456");

        // 执行
        var result = fallback.createRestaurant(request);

        // 验证
        assertNotNull(result);
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), result.getCode());
        assertEquals("服务暂时不可用，请稍后重试", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testDeleteRestaurant_Fallback() {
        // 执行
        var result = fallback.deleteRestaurant(1L);

        // 验证
        assertNotNull(result);
        assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), result.getCode());
        assertEquals("服务暂时不可用，请稍后重试", result.getMessage());
    }
}
