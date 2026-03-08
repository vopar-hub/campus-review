package com.vapor.review.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.vapor.common.enums.ReviewStatus;
import com.vapor.common.error.BizException;
import com.vapor.common.error.ErrorCode;
import com.vapor.common.util.UserContextUtil;
import com.vapor.model.review.ReviewDTO;
import com.vapor.review.entity.ReviewEntity;
import com.vapor.review.mapper.ReviewMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评价后台管理应用服务。
 *
 * 提供待审核列表查询以及通过/驳回等审核能力，并在服务内做最小权限校验。
 */
@Service
public class ReviewAdminService {
    private static final Logger log = LoggerFactory.getLogger(ReviewAdminService.class);

    private final ReviewMapper reviewMapper;

    /**
     * 构造应用服务。
     *
     * @param reviewMapper 评价数据访问组件
     */
    public ReviewAdminService(ReviewMapper reviewMapper) {
        this.reviewMapper = reviewMapper;
    }

    /**
     * 通过评价。
     *
     * @param reviewId 评价 ID
     * @throws BizException 非管理员或评价不存在时抛出
     */
    @Transactional
    public void approve(Long reviewId) {
        UserContextUtil.requireAdmin();
        Long adminId = UserContextUtil.requireUserId();
        log.info("通过评价：reviewId={}, adminId={}", reviewId, adminId);
        updateStatus(reviewId, ReviewStatus.APPROVED);
    }

    /**
     * 驳回评价。
     *
     * @param reviewId 评价 ID
     * @throws BizException 非管理员或评价不存在时抛出
     */
    @Transactional
    public void reject(Long reviewId) {
        UserContextUtil.requireAdmin();
        Long adminId = UserContextUtil.requireUserId();
        log.info("驳回评价：reviewId={}, adminId={}", reviewId, adminId);
        updateStatus(reviewId, ReviewStatus.REJECTED);
    }

    /**
     * 查询待审核评价列表。
     *
     * @return 待审核评价列表（按 ID 升序）
     * @throws BizException 非管理员时抛出
     */
    public List<ReviewDTO> pending() {
        UserContextUtil.requireAdmin();
        return reviewMapper.selectList(new LambdaQueryWrapper<ReviewEntity>()
                        .eq(ReviewEntity::getStatus, ReviewStatus.PENDING.getCode())
                        .orderByAsc(ReviewEntity::getId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 更新评价状态。
     *
     * @param reviewId 评价 ID
     * @param status 新状态
     * @throws BizException 评价不存在时抛出
     */
    private void updateStatus(Long reviewId, ReviewStatus status) {
        int updated = reviewMapper.update(new LambdaUpdateWrapper<ReviewEntity>()
                .eq(ReviewEntity::getId, reviewId)
                .set(ReviewEntity::getStatus, status.getCode()));
        if (updated == 0) {
            throw new BizException(ErrorCode.NOT_FOUND, "评价不存在");
        }
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
