package com.vapor.interaction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vapor.common.error.BizException;
import com.vapor.common.error.ErrorCode;
import com.vapor.common.util.UserContextUtil;
import com.vapor.interaction.entity.FavoriteEntity;
import com.vapor.interaction.entity.LikeEntity;
import com.vapor.interaction.mapper.FavoriteMapper;
import com.vapor.interaction.mapper.LikeMapper;
import com.vapor.model.interaction.InteractRequest;
import com.vapor.model.interaction.InteractionCountDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 互动应用服务。
 *
 * 负责点赞与收藏等互动操作，并提供互动计数查询。
 */
@Service
public class InteractionAppService {
    private static final Logger log = LoggerFactory.getLogger(InteractionAppService.class);

    private final LikeMapper likeMapper;
    private final FavoriteMapper favoriteMapper;

    /**
     * 构造应用服务。
     *
     * @param likeMapper 点赞数据访问组件
     * @param favoriteMapper 收藏数据访问组件
     */
    public InteractionAppService(LikeMapper likeMapper, FavoriteMapper favoriteMapper) {
        this.likeMapper = likeMapper;
        this.favoriteMapper = favoriteMapper;
    }

    /**
     * 点赞目标对象（幂等）。
     *
     * @param request 互动请求
     * @throws BizException 未登录或请求非法时抛出
     */
    @Transactional
    public void like(InteractRequest request) {
        Long userId = UserContextUtil.requireUserId();
        log.info("点赞：userId={}, targetType={}, targetId={}", userId, request.targetType(), request.targetId());

        boolean exists = likeMapper.exists(new LambdaQueryWrapper<LikeEntity>()
                .eq(LikeEntity::getUserId, userId)
                .eq(LikeEntity::getTargetType, normalizeType(request.targetType()))
                .eq(LikeEntity::getTargetId, request.targetId()));
        if (exists) {
            log.debug("点赞已存在，忽略：userId={}, targetType={}, targetId={}", userId, request.targetType(), request.targetId());
            return;
        }
        LikeEntity entity = new LikeEntity();
        entity.setUserId(userId);
        entity.setTargetType(normalizeType(request.targetType()));
        entity.setTargetId(request.targetId());
        entity.setCreatedAt(Instant.now());
        likeMapper.insert(entity);
    }

    /**
     * 取消点赞目标对象（幂等）。
     *
     * @param request 互动请求
     * @throws BizException 未登录或请求非法时抛出
     */
    @Transactional
    public void unlike(InteractRequest request) {
        Long userId = UserContextUtil.requireUserId();
        log.info("取消点赞：userId={}, targetType={}, targetId={}", userId, request.targetType(), request.targetId());

        likeMapper.delete(new LambdaQueryWrapper<LikeEntity>()
                .eq(LikeEntity::getUserId, userId)
                .eq(LikeEntity::getTargetType, normalizeType(request.targetType()))
                .eq(LikeEntity::getTargetId, request.targetId()));
    }

    /**
     * 收藏目标对象（幂等）。
     *
     * @param request 互动请求
     * @throws BizException 未登录或请求非法时抛出
     */
    @Transactional
    public void favorite(InteractRequest request) {
        Long userId = UserContextUtil.requireUserId();
        log.info("收藏：userId={}, targetType={}, targetId={}", userId, request.targetType(), request.targetId());

        boolean exists = favoriteMapper.exists(new LambdaQueryWrapper<FavoriteEntity>()
                .eq(FavoriteEntity::getUserId, userId)
                .eq(FavoriteEntity::getTargetType, normalizeType(request.targetType()))
                .eq(FavoriteEntity::getTargetId, request.targetId()));
        if (exists) {
            log.debug("收藏已存在，忽略：userId={}, targetType={}, targetId={}", userId, request.targetType(), request.targetId());
            return;
        }
        FavoriteEntity entity = new FavoriteEntity();
        entity.setUserId(userId);
        entity.setTargetType(normalizeType(request.targetType()));
        entity.setTargetId(request.targetId());
        entity.setCreatedAt(Instant.now());
        favoriteMapper.insert(entity);
    }

    /**
     * 取消收藏目标对象（幂等）。
     *
     * @param request 互动请求
     * @throws BizException 未登录或请求非法时抛出
     */
    @Transactional
    public void unfavorite(InteractRequest request) {
        Long userId = UserContextUtil.requireUserId();
        log.info("取消收藏：userId={}, targetType={}, targetId={}", userId, request.targetType(), request.targetId());

        favoriteMapper.delete(new LambdaQueryWrapper<FavoriteEntity>()
                .eq(FavoriteEntity::getUserId, userId)
                .eq(FavoriteEntity::getTargetType, normalizeType(request.targetType()))
                .eq(FavoriteEntity::getTargetId, request.targetId()));
    }

    /**
     * 查询目标对象的互动计数。
     *
     * @param targetType 目标类型
     * @param targetId 目标 ID
     * @return 互动计数（点赞数与收藏数）
     */
    public InteractionCountDTO count(String targetType, Long targetId) {
        String type = normalizeType(targetType);
        long likeCount = likeMapper.selectCount(new LambdaQueryWrapper<LikeEntity>()
                .eq(LikeEntity::getTargetType, type)
                .eq(LikeEntity::getTargetId, targetId));
        long favoriteCount = favoriteMapper.selectCount(new LambdaQueryWrapper<FavoriteEntity>()
                .eq(FavoriteEntity::getTargetType, type)
                .eq(FavoriteEntity::getTargetId, targetId));
        return new InteractionCountDTO(type, targetId, likeCount, favoriteCount);
    }

    /**
     * 规范化目标类型输入。
     *
     * @param targetType 目标类型
     * @return 小写且去空白后的目标类型
     * @throws BizException targetType 为空时抛出
     */
    private String normalizeType(String targetType) {
        if (targetType == null || targetType.isBlank()) {
            throw new BizException(ErrorCode.BAD_REQUEST, "targetType 不能为空");
        }
        return targetType.trim().toLowerCase();
    }
}
