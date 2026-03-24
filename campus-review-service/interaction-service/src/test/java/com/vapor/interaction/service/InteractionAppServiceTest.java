package com.vapor.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vapor.common.error.BizException;
import com.vapor.common.util.UserContextUtil;
import com.vapor.common.web.UserContext;
import com.vapor.interaction.entity.FavoriteEntity;
import com.vapor.interaction.entity.LikeEntity;
import com.vapor.interaction.mapper.FavoriteMapper;
import com.vapor.interaction.mapper.LikeMapper;
import com.vapor.model.interaction.InteractRequest;
import com.vapor.model.interaction.InteractionCountDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * InteractionAppService 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class InteractionAppServiceTest {

    @Mock
    private LikeMapper likeMapper;

    @Mock
    private FavoriteMapper favoriteMapper;

    @InjectMocks
    private InteractionAppService interactionService;

    private InteractRequest likeRequest;
    private InteractRequest favoriteRequest;

    @BeforeEach
    void setUp() {
        likeRequest = new InteractRequest("restaurant", 1L);
        favoriteRequest = new InteractRequest("restaurant", 1L);
    }

    @Test
    @DisplayName("点赞成功")
    void like_success() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserId).thenReturn(1L);
            when(likeMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);
            when(likeMapper.insert(any(LikeEntity.class))).thenReturn(1);

            // When
            interactionService.like(likeRequest);

            // Then
            verify(likeMapper).exists(any(LambdaQueryWrapper.class));
            verify(likeMapper).insert(any(LikeEntity.class));
        }
    }

    @Test
    @DisplayName("点赞 - 幂等处理")
    void like_idempotent() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserId).thenReturn(1L);
            when(likeMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(true);

            // When
            interactionService.like(likeRequest);

            // Then
            verify(likeMapper).exists(any(LambdaQueryWrapper.class));
            verify(likeMapper, never()).insert(any(LikeEntity.class));
        }
    }

    @Test
    @DisplayName("取消点赞成功")
    void unlike_success() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserId).thenReturn(1L);
            when(likeMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);

            // When
            interactionService.unlike(likeRequest);

            // Then
            verify(likeMapper).delete(any(LambdaQueryWrapper.class));
        }
    }

    @Test
    @DisplayName("收藏成功")
    void favorite_success() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserId).thenReturn(1L);
            when(favoriteMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);
            when(favoriteMapper.insert(any(FavoriteEntity.class))).thenReturn(1);

            // When
            interactionService.favorite(favoriteRequest);

            // Then
            verify(favoriteMapper).exists(any(LambdaQueryWrapper.class));
            verify(favoriteMapper).insert(any(FavoriteEntity.class));
        }
    }

    @Test
    @DisplayName("收藏 - 幂等处理")
    void favorite_idempotent() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserId).thenReturn(1L);
            when(favoriteMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(true);

            // When
            interactionService.favorite(favoriteRequest);

            // Then
            verify(favoriteMapper).exists(any(LambdaQueryWrapper.class));
            verify(favoriteMapper, never()).insert(any(FavoriteEntity.class));
        }
    }

    @Test
    @DisplayName("取消收藏成功")
    void unfavorite_success() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserId).thenReturn(1L);
            when(favoriteMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);

            // When
            interactionService.unfavorite(favoriteRequest);

            // Then
            verify(favoriteMapper).delete(any(LambdaQueryWrapper.class));
        }
    }

    @Test
    @DisplayName("查询互动计数")
    void count_success() {
        // Given
        when(likeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
        when(favoriteMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        // When
        InteractionCountDTO result = interactionService.count("restaurant", 1L);

        // Then
        assertNotNull(result);
        assertEquals("restaurant", result.targetType());
        assertEquals(1L, result.targetId());
        assertEquals(5, result.likeCount());
        assertEquals(3, result.favoriteCount());
    }

    @Test
    @DisplayName("查询互动计数 - 无互动")
    void count_zero() {
        // Given
        when(likeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(favoriteMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // When
        InteractionCountDTO result = interactionService.count("restaurant", 1L);

        // Then
        assertNotNull(result);
        assertEquals(0, result.likeCount());
        assertEquals(0, result.favoriteCount());
    }

    @Test
    @DisplayName("规范化目标类型 - 大写转小写")
    void normalizeType_uppercase() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserId).thenReturn(1L);
            when(likeMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);
            when(likeMapper.insert(any(LikeEntity.class))).thenReturn(1);

            // When
            interactionService.like(new InteractRequest("RESTAURANT", 1L));

            // Then
            verify(likeMapper).insert(any(LikeEntity.class));
        }
    }

    @Test
    @DisplayName("规范化目标类型 - 带空格")
    void normalizeType_withSpaces() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserId).thenReturn(1L);
            when(likeMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);
            when(likeMapper.insert(any(LikeEntity.class))).thenReturn(1);

            // When
            interactionService.like(new InteractRequest("  restaurant  ", 1L));

            // Then
            verify(likeMapper).insert(any(LikeEntity.class));
        }
    }

    @Test
    @DisplayName("规范化目标类型 - 空字符串抛出异常")
    void normalizeType_emptyString() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserId).thenReturn(1L);

            // When & Then
            BizException exception = assertThrows(BizException.class,
                () -> interactionService.like(new InteractRequest("", 1L)));
            assertEquals("targetType 不能为空", exception.getMessage());
        }
    }

    @Test
    @DisplayName("规范化目标类型 - null 抛出异常")
    void normalizeType_null() {
        try (MockedStatic<UserContextUtil> mocked = mockStatic(UserContextUtil.class)) {
            mocked.when(UserContextUtil::requireUserId).thenReturn(1L);

            // When & Then
            BizException exception = assertThrows(BizException.class,
                () -> interactionService.like(new InteractRequest(null, 1L)));
            assertEquals("targetType 不能为空", exception.getMessage());
        }
    }
}
