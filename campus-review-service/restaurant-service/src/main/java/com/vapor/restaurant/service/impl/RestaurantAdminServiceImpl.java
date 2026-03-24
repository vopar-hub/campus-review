package com.vapor.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vapor.common.error.BizException;
import com.vapor.common.error.ErrorCode;
import com.vapor.common.util.UserContextUtil;
import com.vapor.model.restaurant.RestaurantCreateRequest;
import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.restaurant.entity.RestaurantEntity;
import com.vapor.restaurant.mapper.RestaurantMapper;
import com.vapor.restaurant.service.RestaurantAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 餐厅后台管理服务实现类。
 */
@Service
public class RestaurantAdminServiceImpl implements RestaurantAdminService {
    private static final Logger log = LoggerFactory.getLogger(RestaurantAdminServiceImpl.class);

    private final RestaurantMapper restaurantMapper;

    /**
     * 构造应用服务。
     *
     * @param restaurantMapper 餐厅数据访问组件
     */
    public RestaurantAdminServiceImpl(RestaurantMapper restaurantMapper) {
        this.restaurantMapper = restaurantMapper;
    }

    @Override
    public List<RestaurantDTO> getRestaurantList() {
        UserContextUtil.requireAdmin();
        Long adminId = UserContextUtil.requireUserId();
        log.info("获取餐厅列表：adminId={}", adminId);

        LambdaQueryWrapper<RestaurantEntity> qw = new LambdaQueryWrapper<>();
        qw.orderByDesc(RestaurantEntity::getId);
        List<RestaurantEntity> restaurants = restaurantMapper.selectList(qw);
        return restaurants.stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public RestaurantDTO create(RestaurantCreateRequest request) {
        UserContextUtil.requireAdmin();
        Long adminId = UserContextUtil.requireUserId();
        log.info("创建餐厅：name={}, campus={}, adminId={}", request.name(), adminId);

        RestaurantEntity entity = new RestaurantEntity();
        entity.setName(request.name());
        entity.setCampus(request.campus());
        entity.setAddress(request.address());
        entity.setDescription(request.description());
        entity.setCoverImageUrl(request.coverImageUrl());
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        restaurantMapper.insert(entity);
        return toDTO(entity);
    }

    @Override
    @Transactional
    public void delete(Long restaurantId) {
        UserContextUtil.requireAdmin();
        Long adminId = UserContextUtil.requireUserId();
        log.info("删除餐厅：restaurantId={}, adminId={}", restaurantId, adminId);

        int deleted = restaurantMapper.deleteById(restaurantId);
        if (deleted == 0) {
            throw new BizException(ErrorCode.NOT_FOUND, "餐厅不存在");
        }
    }

    /**
     * 将实体转换为 DTO。
     *
     * @param entity 餐厅实体
     * @return 餐厅 DTO
     */
    private RestaurantDTO toDTO(RestaurantEntity entity) {
        return new RestaurantDTO(
                entity.getId(),
                entity.getName(),
                entity.getCampus(),
                entity.getAddress(),
                entity.getDescription(),
                entity.getCoverImageUrl(),
                entity.getCreatedAt()
        );
    }
}
