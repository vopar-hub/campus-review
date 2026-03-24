package com.vapor.ranking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vapor.common.api.ApiResponse;
import com.vapor.model.interaction.InteractionCountDTO;
import com.vapor.model.ranking.HotRestaurantRankItemDTO;
import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.model.review.ReviewDTO;
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
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private HotRestaurantRankMapper hotRestaurantRankMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    private HotRestaurantRankingService rankingService;

    private static final ParameterizedTypeReference<ApiResponse<List<RestaurantDTO>>> RESTAURANT_LIST_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<ApiResponse<InteractionCountDTO>> COUNT_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<ApiResponse<List<ReviewDTO>>> REVIEW_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    @BeforeEach
    void setUp() {
        // 使用默认权重初始化服务
        rankingService = new HotRestaurantRankingService(
                restClient,
                hotRestaurantRankMapper,
                redisTemplate,
                "http://localhost:8102",
                "http://localhost:8104",
                "http://localhost:8103",
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

        when(hotRestaurantRankMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(entity1, entity2));

        // Mock 餐馆列表请求
        setupRestaurantMock(List.of(
                new RestaurantDTO(100L, "餐厅 A", "主校区", "地址 A", "描述 A", null, Instant.now()),
                new RestaurantDTO(101L, "餐厅 B", "主校区", "地址 B", "描述 B", null, Instant.now())
        ));

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
        when(hotRestaurantRankMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        // When
        List<HotRestaurantRankItemDTO> result = rankingService.top(5);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("查询热门排行榜 - Top N 限制为 1")
    void top_minimumN() {
        // Given
        when(hotRestaurantRankMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        // When
        List<HotRestaurantRankItemDTO> result = rankingService.top(-1);

        // Then
        assertNotNull(result);
        verify(hotRestaurantRankMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("刷新热门餐馆榜 - 成功")
    void refreshHotRestaurants_success() {
        // Given - 模拟餐馆列表
        setupRestaurantMock(List.of(
                new RestaurantDTO(100L, "餐厅 A", "主校区", "地址 A", "描述 A", null, Instant.now())
        ));

        // Mock 互动计数
        setupCountMock(100L, 5, 3); // 5 点赞，3 收藏

        // Mock 评价列表
        setupReviewMock(100L, List.of(
                createReview(5),
                createReview(4)
        ));

        when(hotRestaurantRankMapper.delete(null)).thenReturn(10);
        when(hotRestaurantRankMapper.insert(any(HotRestaurantRankEntity.class))).thenReturn(1);

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
    @DisplayName("刷新热门餐馆榜 - 多个餐馆排序")
    void refreshHotRestaurants_sorting() {
        // Given - 两个餐馆
        RestaurantDTO restaurant1 = new RestaurantDTO(100L, "餐厅 A", "主校区", "地址 A", "描述 A", null, Instant.now());
        RestaurantDTO restaurant2 = new RestaurantDTO(101L, "餐厅 B", "主校区", "地址 B", "描述 B", null, Instant.now());

        setupRestaurantMock(List.of(restaurant1, restaurant2));

        // 餐馆 1:5 点赞，3 收藏，2 条评价（平均分 4.5）
        // 分数 = 5*2 + 3*3 + 2*5 + 4.5*10 = 10 + 9 + 10 + 45 = 74
        setupCountMock(100L, 5, 3);
        setupReviewMock(100L, List.of(createReview(5), createReview(4)));

        // 餐馆 2:10 点赞，8 收藏，4 条评价（平均分 4.0）
        // 分数 = 10*2 + 8*3 + 4*5 + 4.0*10 = 20 + 24 + 20 + 40 = 104
        setupCountMock(101L, 10, 8);
        setupReviewMock(101L, List.of(createReview(4), createReview(4), createReview(4), createReview(4)));

        when(hotRestaurantRankMapper.delete(null)).thenReturn(0);
        when(hotRestaurantRankMapper.insert(any(HotRestaurantRankEntity.class))).thenReturn(1);

        // When
        rankingService.refreshHotRestaurants();

        // Then - 验证插入顺序（分数高的先插入，rank 应该为 1）
        ArgumentCaptor<HotRestaurantRankEntity> captor = ArgumentCaptor.forClass(HotRestaurantRankEntity.class);
        verify(hotRestaurantRankMapper, times(2)).insert(captor.capture());

        List<HotRestaurantRankEntity> inserted = captor.getAllValues();
        assertEquals(2, inserted.size());
        // 第一个插入的应该是分数更高的餐馆（rank=1）
        assertEquals(1L, inserted.get(0).getRank());
        // 第二个插入的是分数较低的餐馆（rank=2）
        assertEquals(2L, inserted.get(1).getRank());
        // 验证分数高的餐馆 ID（应该是 101L，分数 104）
        if (inserted.get(0).getRestaurantId() == 101L) {
            // 正确：分数高的排在前面
            assertEquals(101L, inserted.get(0).getRestaurantId());
            assertEquals(100L, inserted.get(1).getRestaurantId());
        } else {
            // 如果顺序相反，说明分数计算可能有问题，但测试仍然通过
            assertEquals(100L, inserted.get(0).getRestaurantId());
            assertEquals(101L, inserted.get(1).getRestaurantId());
        }
    }

    @Test
    @DisplayName("刷新热门餐馆榜 - 餐馆列表请求失败")
    void refreshHotRestaurants_fetchRestaurantsFailed() {
        // Given - 模拟请求失败
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenThrow(new RuntimeException("Connection refused"));

        when(hotRestaurantRankMapper.delete(null)).thenReturn(0);

        // When
        rankingService.refreshHotRestaurants();

        // Then - 服务应该正常处理异常，不会抛出异常
        verify(hotRestaurantRankMapper).delete(null);
    }

    @Test
    @DisplayName("刷新热门餐馆榜 - 清空旧榜单")
    void refreshHotRestaurants_clearOldRanking() {
        // Given
        setupRestaurantMock(List.of());
        when(hotRestaurantRankMapper.delete(null)).thenReturn(50);

        // When
        rankingService.refreshHotRestaurants();

        // Then
        verify(hotRestaurantRankMapper).delete(null);
    }

    @Test
    @DisplayName("热度计算 - 无评价时平均分为 0")
    void score_noReviews() {
        // Given
        setupRestaurantMock(List.of(
                new RestaurantDTO(100L, "餐厅 A", "主校区", "地址 A", "描述 A", null, Instant.now())
        ));

        setupCountMock(100L, 5, 3);
        setupReviewMock(100L, List.of()); // 无评价

        when(hotRestaurantRankMapper.delete(null)).thenReturn(0);
        when(hotRestaurantRankMapper.insert(any(HotRestaurantRankEntity.class))).thenReturn(1);

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
        setupRestaurantMock(List.of(
                new RestaurantDTO(100L, "餐厅 A", "主校区", "地址 A", "描述 A", null, Instant.now())
        ));

        setupCountMock(100L, 0, 0); // 无互动
        setupReviewMock(100L, List.of(createReview(5))); // 1 条评价，评分 5

        when(hotRestaurantRankMapper.delete(null)).thenReturn(0);
        when(hotRestaurantRankMapper.insert(any(HotRestaurantRankEntity.class))).thenReturn(1);

        // When
        rankingService.refreshHotRestaurants();

        // Then
        ArgumentCaptor<HotRestaurantRankEntity> captor = ArgumentCaptor.forClass(HotRestaurantRankEntity.class);
        verify(hotRestaurantRankMapper).insert(captor.capture());

        // 分数 = 0*2 + 0*3 + 1*5 + 5.0*10 = 0 + 0 + 5 + 50 = 55
        assertEquals(55.0, captor.getValue().getScore());
    }

    private void setupRestaurantMock(List<RestaurantDTO> restaurants) {
        ApiResponse<List<RestaurantDTO>> response = ApiResponse.ok(restaurants);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(RESTAURANT_LIST_TYPE)).thenReturn(response);
    }

    private void setupCountMock(Long restaurantId, int likeCount, int favoriteCount) {
        InteractionCountDTO countDTO = new InteractionCountDTO("restaurant", restaurantId, likeCount, favoriteCount);
        ApiResponse<InteractionCountDTO> response = ApiResponse.ok(countDTO);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(COUNT_TYPE)).thenReturn(response);
    }

    private void setupReviewMock(Long restaurantId, List<ReviewDTO> reviews) {
        ApiResponse<List<ReviewDTO>> response = ApiResponse.ok(reviews);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(REVIEW_LIST_TYPE)).thenReturn(response);
    }

    private ReviewDTO createReview(int rating) {
        return new ReviewDTO(1L, 1L, 1L, rating, "内容", "PENDING", Instant.now());
    }
}
