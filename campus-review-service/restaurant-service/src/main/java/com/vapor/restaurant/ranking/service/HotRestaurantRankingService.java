package com.vapor.restaurant.ranking.service;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.interaction.InteractionCountDTO;
import com.vapor.model.ranking.HotRestaurantRankItemDTO;
import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.model.review.ReviewDTO;
import com.vapor.restaurant.ranking.client.InteractionServiceClient;
import com.vapor.restaurant.ranking.client.ReviewServiceClient;
import com.vapor.restaurant.ranking.entity.HotRestaurantRankEntity;
import com.vapor.restaurant.ranking.mapper.HotRestaurantRankMapper;
import com.vapor.restaurant.ranking.service.RestaurantRankingDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 热门餐馆排行榜服务。
 *
 * 定时从下游服务拉取餐馆、互动与评价数据，根据权重计算热度并落库。
 * 排行榜数据同时存储到数据库和 Redis ZSet 中，查询时直接从 Redis 获取。
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
    private final int minReviewCount;
    private final double minRating;
    private final String redisKey;

    private final RestaurantRankingDataService restaurantRankingDataService;
    private final InteractionServiceClient interactionServiceClient;
    private final ReviewServiceClient reviewServiceClient;
    private final HotRestaurantRankMapper hotRestaurantRankMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * 构造服务。
     *
     * @param hotRestaurantRankMapper 排行榜数据访问组件
     * @param redisTemplate Redis 模板
     * @param restaurantRankingDataService 餐馆排行榜数据服务（本地调用）
     * @param interactionServiceClient 互动服务 Feign 客户端（指向 review-service）
     * @param reviewServiceClient 评价服务 Feign 客户端
     * @param likeWeight 点赞权重（从配置文件读取，默认 2.0）
     * @param favoriteWeight 收藏权重（从配置文件读取，默认 3.0）
     * @param reviewWeight 评价权重（从配置文件读取，默认 5.0）
     * @param ratingWeight 评分权重（从配置文件读取，默认 10.0）
     * @param minReviewCount 最小评论数要求（从配置文件读取，默认 5）
     * @param minRating 最小评分要求（从配置文件读取，默认 3.0）
     * @param redisKey Redis 中存储排行榜的 key（从配置文件读取）
     */
    public HotRestaurantRankingService(
            HotRestaurantRankMapper hotRestaurantRankMapper,
            StringRedisTemplate redisTemplate,
            RestaurantRankingDataService restaurantRankingDataService,
            InteractionServiceClient interactionServiceClient,
            ReviewServiceClient reviewServiceClient,
            @Value("${ranking.hot-restaurants.like-weight:2.0}") double likeWeight,
            @Value("${ranking.hot-restaurants.favorite-weight:3.0}") double favoriteWeight,
            @Value("${ranking.hot-restaurants.review-weight:5.0}") double reviewWeight,
            @Value("${ranking.hot-restaurants.rating-weight:10.0}") double ratingWeight,
            @Value("${ranking.hot-restaurants.min-review-count:5}") int minReviewCount,
            @Value("${ranking.hot-restaurants.min-rating:3.0}") double minRating,
            @Value("${ranking.hot-restaurants.redis-key:ranking:hot-restaurants}") String redisKey
    ) {
        this.hotRestaurantRankMapper = hotRestaurantRankMapper;
        this.redisTemplate = redisTemplate;
        this.restaurantRankingDataService = restaurantRankingDataService;
        this.interactionServiceClient = interactionServiceClient;
        this.reviewServiceClient = reviewServiceClient;
        this.likeWeight = likeWeight;
        this.favoriteWeight = favoriteWeight;
        this.reviewWeight = reviewWeight;
        this.ratingWeight = ratingWeight;
        this.minReviewCount = minReviewCount;
        this.minRating = minRating;
        this.redisKey = redisKey;
    }

    /**
     * 定时刷新热门餐馆榜数据。
     *
     * 刷新策略：拉取全量餐馆列表，对每个餐馆计算热度分并按分数降序排序，最终写入榜单表。
     * 进入排行榜的标准：
     * 1. 评论数大于 5 条
     * 2. 评分大于 3 分
     */
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

        // 写入新榜单到数据库
        Instant now = Instant.now();
        long rank = 1;
        for (ScoredRestaurant item : scored) {
            HotRestaurantRankEntity entity = new HotRestaurantRankEntity();
            entity.setRestaurantId(item.restaurant().id());
            entity.setScore(item.score());
            entity.setAvgRating(item.avgRating());
            entity.setRank(rank++);
            entity.setUpdatedAt(now);
            hotRestaurantRankMapper.insert(entity);
        }

        // 写入 Redis ZSet
        updateRedisRanking(scored);

        log.info("热门餐馆排行榜刷新完成，共 {} 家餐馆符合条件", scored.size());
    }

    /**
     * 将排行榜数据更新到 Redis ZSet。
     * 使用 long 类型存储分数以避免 double 精度问题，同时设置 24 小时过期时间。
     *
     * @param scored 评分后的餐馆列表（已排序）
     */
    private void updateRedisRanking(List<ScoredRestaurant> scored) {
        // 删除旧榜单
        redisTemplate.delete(redisKey);

        // 使用 ZSet 存储排行榜，score 作为排序依据
        // 将 double 分数转换为 long，避免精度问题：score * 10000 保留 4 位小数
        Set<ZSetOperations.TypedTuple<String>> tuples = new java.util.LinkedHashSet<>();
        for (ScoredRestaurant item : scored) {
            String member = String.valueOf(item.restaurant().id());
            long scoreAsLong = (long) Math.round(item.score() * 10000);
            tuples.add(new ZSetOperations.TypedTuple<String>() {
                @Override
                public String getValue() {
                    return member;
                }
                @Override
                public Double getScore() {
                    return (double) scoreAsLong;
                }
                @Override
                public int compareTo(ZSetOperations.TypedTuple<String> other) {
                    return Long.compare(scoreAsLong, (long) Math.round(other.getScore() != null ? other.getScore() : 0));
                }
            });
        }

        if (!tuples.isEmpty()) {
            redisTemplate.opsForZSet().add(redisKey, tuples);
            // 设置 24 小时过期时间，防止缓存雪崩
            redisTemplate.expire(redisKey, 24, java.util.concurrent.TimeUnit.HOURS);
            log.info("Redis 排行榜已更新，共 {} 家餐馆，过期时间 24 小时", tuples.size());
        }
    }

    /**
     * 查询热门餐馆榜 Top N。
     * 直接从 Redis ZSet 中获取数据，同时从数据库获取平均评分。
     * 使用空值缓存防止缓存穿透。
     *
     * @param topN 返回 Top N，最小为 1
     * @return 排行榜条目
     */
    public List<HotRestaurantRankItemDTO> top(int topN) {
        log.debug("查询热门餐馆榜 Top {}", topN);

        // 从 Redis ZSet 获取排名
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(redisKey, 0, Math.max(1, topN) - 1);

        // 缓存穿透防护：如果 Redis 中无数据，返回空列表并记录空值缓存
        if (tuples == null || tuples.isEmpty()) {
            log.debug("Redis 中暂无排行榜数据，设置空值缓存 5 分钟");
            // 设置空值缓存 5 分钟，防止恶意查询穿透到数据库
            String emptyCacheKey = redisKey + ":empty";
            redisTemplate.opsForValue().set(emptyCacheKey, "1", 5, java.util.concurrent.TimeUnit.MINUTES);
            return List.of();
        }

        // 获取所有餐馆 ID
        List<Long> restaurantIds = tuples.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .filter(Objects::nonNull)
                .map(Long::parseLong)
                .toList();

        // 批量获取餐馆信息
        List<RestaurantDTO> restaurants = fetchRestaurantsByIds(restaurantIds);

        // 从数据库获取平均评分信息
        List<HotRestaurantRankEntity> rankEntities = hotRestaurantRankMapper.selectByRestaurantIds(restaurantIds);

        List<HotRestaurantRankItemDTO> result = new java.util.ArrayList<>();
        long rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            Long restaurantId = Long.parseLong(tuple.getValue());
            String restaurantName = restaurants.stream()
                    .filter(r -> Objects.equals(r.id(), restaurantId))
                    .map(RestaurantDTO::name)
                    .findFirst()
                    .orElse("Unknown");
            // 获取平均评分
            double avgRating = rankEntities.stream()
                    .filter(e -> Objects.equals(e.getRestaurantId(), restaurantId))
                    .mapToDouble(HotRestaurantRankEntity::getAvgRating)
                    .findFirst()
                    .orElse(0.0);
            result.add(new HotRestaurantRankItemDTO(
                    rank++,
                    restaurantId,
                    restaurantName,
                    tuple.getScore() != null ? tuple.getScore() : 0.0,
                    avgRating
            ));
        }
        return result;
    }

    /**
     * 拉取餐馆列表。
     *
     * @return 餐馆列表；下游响应为空时返回空列表
     */
    private List<RestaurantDTO> fetchRestaurants() {
        try {
            return restaurantRankingDataService.getAllRestaurants();
        } catch (Exception e) {
            log.error("拉取餐馆列表失败：error={}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 根据 ID 列表批量获取餐馆信息。
     *
     * @param ids 餐馆 ID 列表
     * @return 餐馆列表
     */
    private List<RestaurantDTO> fetchRestaurantsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        try {
            return restaurantRankingDataService.getRestaurantsByIds(ids);
        } catch (Exception e) {
            log.error("批量拉取餐馆列表失败：ids={}, error={}", ids, e.getMessage(), e);
            // 降级：返回全量餐馆列表并过滤
            return fetchRestaurants().stream()
                    .filter(r -> ids.contains(r.id()))
                    .toList();
        }
    }

    /**
     * 计算单个餐馆的热度分。
     * 只有满足以下条件的餐馆才会被计算热度分：
     * 1. 评论数大于 5 条
     * 2. 评分大于 3 分
     *
     * @param restaurant 餐馆信息
     * @return 评分结果；输入非法时或不满足条件时返回 null
     */
    private ScoredRestaurant score(RestaurantDTO restaurant) {
        if (restaurant == null || restaurant.id() == null) {
            return null;
        }

        InteractionCountDTO counts = fetchCounts(restaurant.id());
        List<ReviewDTO> reviews = fetchReviews(restaurant.id());

        // 不满足条件：评论数 <= 5 或 评分 <= 3
        if (reviews.size() <= minReviewCount) {
            log.debug("餐馆 {} 评论数不足，跳过：reviewCount={}", restaurant.id(), reviews.size());
            return null;
        }

        double avgRating = reviews.isEmpty() ? 0.0 : reviews.stream().mapToInt(ReviewDTO::rating).average().orElse(0.0);

        if (avgRating <= minRating) {
            log.debug("餐馆 {} 评分不足，跳过：avgRating={}", restaurant.id(), avgRating);
            return null;
        }

        // 热度分 = 点赞数*权重 + 收藏数*权重 + 评价数*权重 + 平均分*权重
        double score = counts.likeCount() * likeWeight
                     + counts.favoriteCount() * favoriteWeight
                     + reviews.size() * reviewWeight
                     + avgRating * ratingWeight;

        log.debug("计算餐馆热度：restaurantId={}, likeCount={}, favoriteCount={}, reviewCount={}, avgRating={}, score={}",
                restaurant.id(), counts.likeCount(), counts.favoriteCount(), reviews.size(), avgRating, score);

        return new ScoredRestaurant(restaurant, score, avgRating);
    }

    /**
     * 拉取餐馆的互动计数。
     *
     * @param restaurantId 餐馆 ID
     * @return 互动计数；下游响应为空时返回默认值
     */
    private InteractionCountDTO fetchCounts(Long restaurantId) {
        try {
            ApiResponse<InteractionCountDTO> resp = interactionServiceClient.getCount("restaurant", restaurantId);
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
            ApiResponse<List<ReviewDTO>> resp = reviewServiceClient.getReviews(restaurantId);
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
     * @param avgRating 平均评分
     */
    private record ScoredRestaurant(RestaurantDTO restaurant, double score, double avgRating) {
    }
}
