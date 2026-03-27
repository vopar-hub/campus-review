package com.vapor.ranking.client;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.review.ReviewDTO;
import com.vapor.ranking.client.fallback.ReviewServiceClientFallback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 评价服务 Feign 客户端降级测试。
 */
class ReviewServiceClientFallbackTest {

    @InjectMocks
    private ReviewServiceClientFallback fallback;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetReviews_Fallback() {
        // 执行
        ApiResponse<List<ReviewDTO>> result = fallback.getReviews(1L);

        // 验证 - 降级时返回空列表
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
    }
}
