package com.vapor.review.service;

import com.vapor.model.review.ReviewDTO;

import java.util.List;

/**
 * 评价后台管理服务接口。
 *
 * 提供待审核列表查询以及通过/驳回等审核能力。
 */
public interface ReviewAdminService {

    /**
     * 通过评价。
     *
     * @param reviewId 评价 ID
     */
    void approve(Long reviewId);

    /**
     * 驳回评价。
     *
     * @param reviewId 评价 ID
     */
    void reject(Long reviewId);

    /**
     * 查询待审核评价列表。
     *
     * @return 待审核评价列表（按 ID 升序）
     */
    List<ReviewDTO> pending();
}
