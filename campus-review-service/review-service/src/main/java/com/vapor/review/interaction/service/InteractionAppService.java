package com.vapor.review.interaction.service;

import com.vapor.model.interaction.InteractRequest;
import com.vapor.model.interaction.InteractionCountDTO;

/**
 * 互动应用服务接口。
 *
 * 负责点赞与收藏等互动操作，并提供互动计数查询。
 */
public interface InteractionAppService {

    /**
     * 点赞目标对象（幂等）。
     *
     * @param request 互动请求
     */
    void like(InteractRequest request);

    /**
     * 取消点赞目标对象（幂等）。
     *
     * @param request 互动请求
     */
    void unlike(InteractRequest request);

    /**
     * 收藏目标对象（幂等）。
     *
     * @param request 互动请求
     */
    void favorite(InteractRequest request);

    /**
     * 取消收藏目标对象（幂等）。
     *
     * @param request 互动请求
     */
    void unfavorite(InteractRequest request);

    /**
     * 查询目标对象的互动计数。
     *
     * @param targetType 目标类型
     * @param targetId 目标 ID
     * @return 互动计数（点赞数与收藏数）
     */
    InteractionCountDTO count(String targetType, Long targetId);
}
