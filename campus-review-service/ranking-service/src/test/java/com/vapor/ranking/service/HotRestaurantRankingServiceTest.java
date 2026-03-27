package com.vapor.ranking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vapor.common.api.ApiResponse;
import com.vapor.model.interaction.InteractionCountDTO;
import com.vapor.model.ranking.HotRestaurantRankItemDTO;
import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.model.review.ReviewDTO;
import com.vapor.ranking.client.InteractionServiceClient;
import com.vapor.ranking.client.RestaurantServiceClient;
import com.vapor.ranking.client.ReviewServiceClient;
import com.vapor.ranking.entity.HotRestaurantRankEntity;
import com.vapor.ranking.mapper.HotRestaurantRankMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * HotRestaurantRankingService 单元测试。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HotRestaurantRankingServiceTest {

    @Mock
    private RestaurantServiceClient restaurantServiceClient;

    @Mock
    private InteractionServiceClient interactionServiceClient;

    @Mock
    private ReviewServiceClient reviewServiceClient;

    @Mock
    private HotRestaurantRankMapper hotRestaurantRankMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private StringRedisTemplate.Operations valueOps;

    private HotRestaurantRankingService rankingService;

    @BeforeEach
    void setUp() {
        // 使用默认权重初始化服务
        rankingService = new HotRestaurantRankingService(
                hotRestaurantRankMapper,
                redisTemplate,
                restaurantServiceClient,
                interactionServiceClient,
                reviewServiceClient,
                2.0,    // likeWeight
                3.0,    // favoriteWeight
                5.0,    // reviewWeight
                10.0,   // ratingWeight
                5,      // minReviewCount
                3.0,    // minRating
                "ranking:hot-restaurants"  // redisKey
        );
    }

    @Test
    @DisplayName("查询热门排行榜 Top N")
    void top_success() {
        // Given
        HotRestaurantRankEntity entity1 = new HotRestaurantRankEntity();
        entity1.setId(1L);
        entity1.setRestaurantId(100L);
        entity1.setScore(95.0);
        entity1.setRank(1L);
        entity1.setUpdatedAt(Instant.now());

        HotRestaurantRankEntity entity2 = new HotRestaurantRankEntity();
        entity2.setId(2L);
        entity2.setRestaurantId(101L);
        entity2.setScore(85.0);
        entity2.setRank(2L);
        entity2.setUpdatedAt(Instant.now());

        // Mock Redis ZSet
        Set<ZSetOperations.TypedTuple<String>> tuples = Set.of(
                createTypedTuple("100", 95.0),
                createTypedTuple("101", 85.0)
        );
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRangeWithScores(any(), anyLong(), anyLong())).thenReturn(tuples);

        // Mock Redis value operations
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // Mock 餐馆信息
        when(restaurantServiceClient.getRestaurantsByIds(List.of(100L, 101L)))
                .thenReturn(ApiResponse.ok(List.of(
                        new RestaurantDTO(100L, "餐厅 A", "主校区", "地址 A", "描述 A", null, Instant.now()),
                        new RestaurantDTO(101L, "餐厅 B", "主校区", "地址 B", "描述 B", null, Instant.now())
                )));

        when(hotRestaurantRankMapper.selectByRestaurantIds(List.of(100L, 101L)))
                .thenReturn(List.of(entity1, entity2));

        // When
        List<HotRestaurantRankItemDTO> result = rankingService.top(2);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).rank());
        assertEquals("餐厅 A", result.get(0).restaurantName());
        assertEquals(2L, result.get(1).rank());
        assertEquals("餐厅 B", result.get(1).restaurantName());
    }

    @Test
    @DisplayName("查询热门排行榜 - 返回空列表")
    void top_emptyList() {
        // Given
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRangeWithScores(any(), anyLong(), anyLong())).thenReturn(Set.of());

        // When
        List<HotRestaurantRankItemDTO> result = rankingService.top(5);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("刷新热门餐馆榜 - 成功")
    void refreshHotRestaurants_success() {
        // Given - 模拟餐馆列表
        when(restaurantServiceClient.getRestaurants())
                .thenReturn(ApiResponse.ok(List.of(
                        new RestaurantDTO(100L, "餐厅 A", "主校区", "地址 A", "描述 A", null, Instant.now())
                )));

        // Mock 互动计数
        when(interactionServiceClient.getCount("restaurant", 100L))
                .thenReturn(ApiResponse.ok(new InteractionCountDTO("restaurant", 100L, 5, 3)));

        // Mock 评价列表
        when(reviewServiceClient.getReviews(100L))
                .thenReturn(ApiResponse.ok(List.of(
                        createReview(5),
                        createReview(4)
                )));

        when(hotRestaurantRankMapper.delete(null)).thenReturn(10);
        when(hotRestaurantRankMapper.insert(any(HotRestaurantRankEntity.class))).thenReturn(1);

        // Mock Redis
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        doNothing().when(zSetOperations).add(any(), any());
        when(redisTemplate.expire(any(), anyLong(), any())).thenReturn(true);

        // When
        rankingService.refreshHotRestaurants();

        // Then
        ArgumentCaptor<HotRestaurantRankEntity> captor = ArgumentCaptor.forClass(HotRestaurantRankEntity.class);
        verify(hotRestaurantRankMapper).insert(captor.capture());

        HotRestaurantRankEntity inserted = captor.getValue();
        assertNotNull(inserted);
        assertEquals(100L, inserted.getRestaurantId());
        assertNotNull(inserted.getScore());
        assertEquals(1L, inserted.getRank());
        assertNotNull(inserted.getUpdatedAt());
    }

    @Test
    @DisplayName("刷新热门餐馆榜 - 餐馆列表请求失败")
    void refreshHotRestaurants_fetchRestaurantsFailed() {
        // Given - 模拟请求失败
        when(restaurantServiceClient.getRestaurants())
                .thenThrow(new RuntimeException("Connection refused"));

        when(hotRestaurantRankMapper.delete(null)).thenReturn(0);

        // When
        rankingService.refreshHotRestaurants();

        // Then - 服务应该正常处理异常，不会抛出异常
        verify(hotRestaurantRankMapper).delete(null);
    }

    @Test
    @DisplayName("热度计算 - 无评价时平均分为 0")
    void score_noReviews() {
        // Given
        when(restaurantServiceClient.getRestaurants())
                .thenReturn(ApiResponse.ok(List.of(
                        new RestaurantDTO(100L, "餐厅 A", "主校区", "地址 A", "描述 A", null, Instant.now())
                )));

        when(interactionServiceClient.getCount("restaurant", 100L))
                .thenReturn(ApiResponse.ok(new InteractionCountDTO("restaurant", 100L, 5, 3)));

        when(reviewServiceClient.getReviews(100L))
                .thenReturn(ApiResponse.ok(List.of())); // 无评价

        when(hotRestaurantRankMapper.delete(null)).thenReturn(0);
        when(hotRestaurantRankMapper.insert(any(HotRestaurantRankEntity.class))).thenReturn(1);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        // When
        rankingService.refreshHotRestaurants();

        // Then
        ArgumentCaptor<HotRestaurantRankEntity> captor = ArgumentCaptor.forClass(HotRestaurantRankEntity.class);
        verify(hotRestaurantRankMapper).insert(captor.capture());

        // 分数 = 5*2 + 3*3 + 0*5 + 0.0*10 = 10 + 9 + 0 + 0 = 19
        assertEquals(19.0, captor.getValue().getScore());
    }

    @Test
    @DisplayName("热度计算 - 互动计数为 0")
    void score_zeroInteractions() {
        // Given
        when(restaurantServiceClient.getRestaurants())
                .thenReturn(ApiResponse.ok(List.of(
                        new RestaurantDTO(100L, "餐厅 A", "主校区", "地址 A", "描述 A", null, Instant.now())
                )));

        when(interactionServiceClient.getCount("restaurant", 100L))
                .thenReturn(ApiResponse.ok(new InteractionCountDTO("restaurant", 100L, 0, 0)));

        when(reviewServiceClient.getReviews(100L))
                .thenReturn(ApiResponse.ok(List.of(createReview(5)))); // 1 条评价，评分 5

        when(hotRestaurantRankMapper.delete(null)).thenReturn(0);
        when(hotRestaurantRankMapper.insert(any(HotRestaurantRankEntity.class))).thenReturn(1);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        // When
        rankingService.refreshHotRestaurants();

        // Then
        ArgumentCaptor<HotRestaurantRankEntity> captor = ArgumentCaptor.forClass(HotRestaurantRankEntity.class);
        verify(hotRestaurantRankMapper).insert(captor.capture());

        // 分数 = 0*2 + 0*3 + 1*5 + 5.0*10 = 0 + 0 + 5 + 50 = 55
        assertEquals(55.0, captor.getValue().getScore());
    }

    private <T> ZSetOperations.TypedTuple<T> createTypedTuple(T value, double score) {
        return new ZSetOperations.TypedTuple<T>() {
            @Override
            public T getValue() {
                return value;
            }

            @Override
            public Double getScore() {
                return score;
            }

            @Override
            public int compareTo(ZSetOperations.TypedTuple<T> other) {
                return Double.compare(score, other.getScore());
            }
        };
    }

    private ReviewDTO createReview(int rating) {
        return new ReviewDTO(1L, 1L, 1L, rating, "内容", "PENDING", Instant.now());
    }
}
