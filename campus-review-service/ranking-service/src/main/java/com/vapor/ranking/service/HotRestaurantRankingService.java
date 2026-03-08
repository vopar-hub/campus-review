package com.vapor.ranking.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vapor.common.api.ApiResponse;
import com.vapor.model.interaction.InteractionCountDTO;
import com.vapor.model.ranking.HotRestaurantRankItemDTO;
import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.model.review.ReviewDTO;
import com.vapor.ranking.entity.HotRestaurantRankEntity;
import com.vapor.ranking.mapper.HotRestaurantRankMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 热门餐馆排行榜服务。
 *
 * 定时从下游服务拉取餐馆、互动与评价数据，根据权重计算热度并落库。
 */
@Service
public class HotRestaurantRankingService {
    private static final Logger log = LoggerFactory.getLogger(HotRestaurantRankingService.class);

    /**
     * 热度计算权重系数（从配置文件读取）。
     * 评分公式：score = likeCount * LIKE_WEIGHT + favoriteCount * FAVORITE_WEIGHT + reviewCount * REVIEW_WEIGHT + avgRating * RATING_WEIGHT
     */
    private final double likeWeight;
    private final double favoriteWeight;
    private final double reviewWeight;
    private final double ratingWeight;

    private static final ParameterizedTypeReference<ApiResponse<List<RestaurantDTO>>> RESTAURANT_LIST_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<ApiResponse<InteractionCountDTO>> COUNT_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<ApiResponse<List<ReviewDTO>>> REVIEW_LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;
    private final HotRestaurantRankMapper hotRestaurantRankMapper;
    private final String restaurantBaseUrl;
    private final String interactionBaseUrl;
    private final String reviewBaseUrl;

    /**
     * 构造服务。
     *
     * @param restClient HTTP 客户端
     * @param hotRestaurantRankMapper 排行榜数据访问组件
     * @param restaurantBaseUrl 餐馆服务基础地址
     * @param interactionBaseUrl 互动服务基础地址
     * @param reviewBaseUrl 评价服务基础地址
     * @param likeWeight 点赞权重（从配置文件读取，默认 2.0）
     * @param favoriteWeight 收藏权重（从配置文件读取，默认 3.0）
     * @param reviewWeight 评价权重（从配置文件读取，默认 5.0）
     * @param ratingWeight 评分权重（从配置文件读取，默认 10.0）
     */
    public HotRestaurantRankingService(
            RestClient restClient,
            HotRestaurantRankMapper hotRestaurantRankMapper,
            @Value("${downstream.restaurant-service-base-url}") String restaurantBaseUrl,
            @Value("${downstream.interaction-service-base-url}") String interactionBaseUrl,
            @Value("${downstream.review-service-base-url}") String reviewBaseUrl,
            @Value("${ranking.hot-restaurants.like-weight:2.0}") double likeWeight,
            @Value("${ranking.hot-restaurants.favorite-weight:3.0}") double favoriteWeight,
            @Value("${ranking.hot-restaurants.review-weight:5.0}") double reviewWeight,
            @Value("${ranking.hot-restaurants.rating-weight:10.0}") double ratingWeight
    ) {
        this.restClient = restClient;
        this.hotRestaurantRankMapper = hotRestaurantRankMapper;
        this.restaurantBaseUrl = restaurantBaseUrl;
        this.interactionBaseUrl = interactionBaseUrl;
        this.reviewBaseUrl = reviewBaseUrl;
        this.likeWeight = likeWeight;
        this.favoriteWeight = favoriteWeight;
        this.reviewWeight = reviewWeight;
        this.ratingWeight = ratingWeight;
    }

    /**
     * 定时刷新热门餐馆榜数据。
     *
     * 刷新策略：拉取全量餐馆列表，对每个餐馆计算热度分并按分数降序排序，最终写入榜单表。
     */
    @CacheEvict(value = "ranking:hot-restaurants", allEntries = true)
    @Transactional
    @Scheduled(fixedDelayString = "${ranking.hot-restaurants.refresh-ms:60000}")
    public void refreshHotRestaurants() {
        log.info("开始刷新热门餐馆排行榜");

        List<RestaurantDTO> restaurants = fetchRestaurants();
        log.info("拉取到 {} 家餐馆", restaurants.size());

        List<ScoredRestaurant> scored = restaurants.stream()
                .map(this::score)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(ScoredRestaurant::score).reversed())
                .toList();

        // 清空旧榜单
        int deleted = hotRestaurantRankMapper.delete(null);
        log.info("清空旧榜单，删除 {} 条记录", deleted);

        // 写入新榜单
        Instant now = Instant.now();
        long rank = 1;
        for (ScoredRestaurant item : scored) {
            HotRestaurantRankEntity entity = new HotRestaurantRankEntity();
            entity.setRestaurantId(item.restaurant().id());
            entity.setScore(item.score());
            entity.setRank(rank++);
            entity.setUpdatedAt(now);
            hotRestaurantRankMapper.insert(entity);
        }
        log.info("热门餐馆排行榜刷新完成，共 {} 家餐馆", scored.size());
    }

    /**
     * 查询热门餐馆榜 Top N。
     *
     * @param topN 返回 Top N，最小为 1
     * @return 排行榜条目
     */
    @Cacheable(value = "ranking:hot-restaurants", key = "'top:' + #topN", unless = "#result.isEmpty()")
    public List<HotRestaurantRankItemDTO> top(int topN) {
        log.debug("查询热门餐馆榜 Top {}", topN);

        List<HotRestaurantRankEntity> entities = hotRestaurantRankMapper.selectList(new LambdaQueryWrapper<HotRestaurantRankEntity>()
                .orderByAsc(HotRestaurantRankEntity::getRank)
                .last("limit " + Math.max(1, topN)));

        log.debug("查询到 {} 条榜单记录", entities.size());

        // 注意：这里应该缓存餐馆列表避免重复调用
        List<RestaurantDTO> restaurants = fetchRestaurants();
        return entities.stream()
                .map(e -> new HotRestaurantRankItemDTO(
                        e.getRank(),
                        e.getRestaurantId(),
                        restaurants.stream()
                                .filter(r -> Objects.equals(r.id(), e.getRestaurantId()))
                                .map(RestaurantDTO::name)
                                .findFirst()
                                .orElse(null),
                        e.getScore() == null ? 0.0 : e.getScore()
                ))
                .toList();
    }

    /**
     * 拉取餐馆列表。
     *
     * @return 餐馆列表；下游响应为空时返回空列表
     */
    private List<RestaurantDTO> fetchRestaurants() {
        try {
            ApiResponse<List<RestaurantDTO>> resp = restClient.get()
                    .uri(restaurantBaseUrl + "/api/restaurants")
                    .retrieve()
                    .body(RESTAURANT_LIST_TYPE);
            return resp == null || resp.getData() == null ? List.of() : resp.getData();
        } catch (Exception e) {
            log.error("拉取餐馆列表失败：error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 计算单个餐馆的热度分。
     *
     * @param restaurant 餐馆信息
     * @return 评分结果；输入非法时返回 null
     */
    private ScoredRestaurant score(RestaurantDTO restaurant) {
        if (restaurant == null || restaurant.id() == null) {
            return null;
        }

        InteractionCountDTO counts = fetchCounts(restaurant.id());
        List<ReviewDTO> reviews = fetchReviews(restaurant.id());
        double avgRating = reviews.isEmpty() ? 0.0 : reviews.stream().mapToInt(ReviewDTO::rating).average().orElse(0.0);

        // 热度分 = 点赞数*权重 + 收藏数*权重 + 评价数*权重 + 平均分*权重
        double score = counts.likeCount() * likeWeight
                     + counts.favoriteCount() * favoriteWeight
                     + reviews.size() * reviewWeight
                     + avgRating * ratingWeight;

        log.debug("计算餐馆热度：restaurantId={}, likeCount={}, favoriteCount={}, reviewCount={}, avgRating={}, score={}",
                restaurant.id(), counts.likeCount(), counts.favoriteCount(), reviews.size(), avgRating, score);

        return new ScoredRestaurant(restaurant, score);
    }

    /**
     * 拉取餐馆的互动计数。
     *
     * @param restaurantId 餐馆 ID
     * @return 互动计数；下游响应为空时返回默认值
     */
    private InteractionCountDTO fetchCounts(Long restaurantId) {
        try {
            ApiResponse<InteractionCountDTO> resp = restClient.get()
                    .uri(interactionBaseUrl + "/api/interactions/count?targetType=restaurant&targetId=" + restaurantId)
                    .retrieve()
                    .body(COUNT_TYPE);
            return resp == null || resp.getData() == null
                    ? new InteractionCountDTO("restaurant", restaurantId, 0, 0)
                    : resp.getData();
        } catch (Exception e) {
            log.warn("拉取互动计数失败：restaurantId={}, error={}", restaurantId, e.getMessage());
            return new InteractionCountDTO("restaurant", restaurantId, 0, 0);
        }
    }

    /**
     * 拉取餐馆的已通过评价列表。
     *
     * @param restaurantId 餐馆 ID
     * @return 评价列表；下游响应为空时返回空列表
     */
    private List<ReviewDTO> fetchReviews(Long restaurantId) {
        try {
            ApiResponse<List<ReviewDTO>> resp = restClient.get()
                    .uri(reviewBaseUrl + "/api/reviews?restaurantId=" + restaurantId)
                    .retrieve()
                    .body(REVIEW_LIST_TYPE);
            return resp == null || resp.getData() == null ? List.of() : resp.getData();
        } catch (Exception e) {
            log.warn("拉取评价列表失败：restaurantId={}, error={}", restaurantId, e.getMessage());
            return List.of();
        }
    }

    /**
     * 餐馆热度计算中间结果。
     *
     * @param restaurant 餐馆信息
     * @param score 热度分
     */
    private record ScoredRestaurant(RestaurantDTO restaurant, double score) {
    }
}
