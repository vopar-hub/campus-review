package com.vapor.ranking.client;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.interaction.InteractionCountDTO;
import com.vapor.ranking.client.fallback.InteractionServiceClientFallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 互动服务 Feign 客户端降级测试。
 */
class InteractionServiceClientFallbackTest {

    @InjectMocks
    private InteractionServiceClientFallback fallback;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCount_Fallback() {
        // 执行
        ApiResponse<InteractionCountDTO> result = fallback.getCount("restaurant", 1L);

        // 验证 - 降级时返回默认的 0 值
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertNotNull(result.getData());
        assertEquals("restaurant", result.getData().targetType());
        assertEquals(1L, result.getData().targetId());
        assertEquals(0, result.getData().likeCount());
        assertEquals(0, result.getData().favoriteCount());
    }
}
