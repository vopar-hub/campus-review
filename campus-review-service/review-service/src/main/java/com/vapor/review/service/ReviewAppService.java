package com.vapor.review.service;

import com.vapor.model.review.ReviewCreateRequest;
import com.vapor.model.review.ReviewDTO;

import java.util.List;

/**
 * 评价应用服务接口。
 *
 * 负责评价发布、按餐馆查询（仅已通过）与查询我的评价等核心流程。
 */
public interface ReviewAppService {

    /**
     * 发布评价。
     *
     * 需要登录态，创建后状态为 PENDING，等待后台审核。
     *
     * @param request 发布请求
     * @return 创建后的评价信息
     */
    ReviewDTO create(ReviewCreateRequest request);

    /**
     * 查询指定餐馆的已通过评价列表。
     *
     * @param restaurantId 餐馆 ID
     * @return 评价列表
     */
    List<ReviewDTO> listByRestaurant(Long restaurantId);

    /**
     * 查询当前登录用户发布的评价列表。
     *
     * @return 我的评价列表
     */
    List<ReviewDTO> myReviews();
}
