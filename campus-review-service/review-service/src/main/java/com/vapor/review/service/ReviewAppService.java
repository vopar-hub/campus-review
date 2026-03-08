package com.vapor.review.service;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 评价应用服务。
 *
 * 负责评价发布、按餐馆查询（仅已通过）与查询我的评价等核心流程。
 */
@Service
public class ReviewAppService {
    private static final Logger log = LoggerFactory.getLogger(ReviewAppService.class);

    private final ReviewMapper reviewMapper;

    /**
     * 构造应用服务。
     *
     * @param reviewMapper 评价数据访问组件
     */
    public ReviewAppService(ReviewMapper reviewMapper) {
        this.reviewMapper = reviewMapper;
    }

    /**
     * 发布评价。
     *
     * 需要登录态，创建后状态为 PENDING，等待后台审核。
     *
     * @param request 发布请求
     * @return 创建后的评价信息
     * @throws BizException 未登录或输入非法时抛出
     */
    @Transactional
    public ReviewDTO create(ReviewCreateRequest request) {
        UserContext ctx = UserContextHolder.get();
        if (ctx == null || ctx.getUserId() == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "未登录");
        }

        log.info("发布评价：userId={}, restaurantId={}, rating={}", ctx.getUserId(), request.restaurantId(), request.rating());

        Instant now = Instant.now();
        ReviewEntity entity = new ReviewEntity();
        entity.setRestaurantId(request.restaurantId());
        entity.setUserId(ctx.getUserId());
        entity.setRating(request.rating());
        entity.setContent(request.content());
        entity.setStatus(ReviewStatus.PENDING.getCode());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        reviewMapper.insert(entity);

        log.info("评价发布成功：reviewId={}, status=PENDING", entity.getId());
        return toDTO(entity);
    }

    /**
     * 查询指定餐馆的已通过评价列表。
     *
     * @param restaurantId 餐馆 ID
     * @return 评价列表
     * @throws BizException restaurantId 为空时抛出
     */
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

    /**
     * 查询当前登录用户发布的评价列表。
     *
     * @return 我的评价列表
     * @throws BizException 未登录时抛出
     */
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
