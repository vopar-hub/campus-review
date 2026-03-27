package com.vapor.ranking.client;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.ranking.client.fallback.RestaurantServiceClientFallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.List;

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

        // 验证 - 返回空列表作为降级
        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void testGetRestaurantsByIds_Fallback() {
        // 执行
        var result = fallback.getRestaurantsByIds(List.of(1L, 2L));

        // 验证
        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }
}
