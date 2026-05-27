package com.vapor.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vapor.common.enums.ReviewStatus;
import com.vapor.common.error.BizException;
import com.vapor.common.error.ErrorCode;
import com.vapor.common.web.UserContext;
import com.vapor.common.web.UserContextHolder;
import com.vapor.model.review.ReviewCreateRequest;
import com.vapor.model.review.ReviewDTO;
import com.vapor.review.entity.ReviewEntity;
import com.vapor.review.mapper.ReviewMapper;
import com.vapor.review.service.ReviewAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 评价应用服务实现类。
 */
@Service
public class ReviewAppServiceImpl implements ReviewAppService {
    private static final Logger log = LoggerFactory.getLogger(ReviewAppServiceImpl.class);

    private final ReviewMapper reviewMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * 构造应用服务。
     *
     * @param reviewMapper 评价数据访问组件
     * @param redisTemplate Redis 模板
     */
    public ReviewAppServiceImpl(ReviewMapper reviewMapper, StringRedisTemplate redisTemplate) {
        this.reviewMapper = reviewMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public ReviewDTO create(ReviewCreateRequest request) {
        UserContext ctx = UserContextHolder.get();
        if (ctx == null || ctx.getUserId() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "未登录");
        }

        // 限流检查：五分钟内同一用户在同一餐厅最多发送两次评论
        String limitKey = "review:limit:" + ctx.getUserId() + ":" + request.restaurantId();
        Long count = redisTemplate.opsForValue().increment(limitKey);
        if (count != null && count == 1) {
            redisTemplate.expire(limitKey, 5, TimeUnit.MINUTES);
        }
        if (count != null && count > 2) {
            throw new BizException(ErrorCode.TOO_MANY_REQUESTS, "发送评价太频繁");
        }

        log.info("发布评价：userId={}, restaurantId={}, rating={}", ctx.getUserId(), request.restaurantId(), request.rating());

        Instant now = Instant.now();
        ReviewEntity entity = new ReviewEntity();
        entity.setRestaurantId(request.restaurantId());
        entity.setUserId(ctx.getUserId());
        entity.setRating(request.rating());
        entity.setContent(request.content());
        entity.setStatus(ReviewStatus.APPROVED.getCode());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        reviewMapper.insert(entity);

        log.info("评价发布成功：reviewId={}, status=APPROVED", entity.getId());
        return toDTO(entity);
    }

    @Override
    public List<ReviewDTO> listByRestaurant(Long restaurantId) {
        if (restaurantId == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "restaurantId 不能为空");
        }

        return reviewMapper.selectList(new LambdaQueryWrapper<ReviewEntity>()
                        .eq(ReviewEntity::getRestaurantId, restaurantId)
                        .eq(ReviewEntity::getStatus, ReviewStatus.APPROVED.getCode())
                        .orderByDesc(ReviewEntity::getId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public List<ReviewDTO> myReviews() {
        UserContext ctx = UserContextHolder.get();
        if (ctx == null || ctx.getUserId() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "未登录");
        }

        return reviewMapper.selectList(new LambdaQueryWrapper<ReviewEntity>()
                        .eq(ReviewEntity::getUserId, ctx.getUserId())
                        .orderByDesc(ReviewEntity::getId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 将实体映射为对外 DTO。
     *
     * @param entity 评价实体
     * @return 评价 DTO
     */
    private ReviewDTO toDTO(ReviewEntity entity) {
        return new ReviewDTO(
                entity.getId(),
                entity.getRestaurantId(),
                entity.getUserId(),
                entity.getRating(),
                entity.getContent(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
