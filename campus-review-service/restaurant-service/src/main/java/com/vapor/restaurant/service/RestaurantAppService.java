package com.vapor.restaurant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vapor.common.error.BizException;
import com.vapor.common.error.ErrorCode;
import com.vapor.model.restaurant.RestaurantCreateRequest;
import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.restaurant.entity.RestaurantEntity;
import com.vapor.restaurant.mapper.RestaurantMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 餐馆应用服务。
 *
 * 负责餐馆创建、查询与检索等核心业务，并完成实体与 DTO 的转换。
 */
@Service
public class RestaurantAppService {
    private final RestaurantMapper restaurantMapper;

    /**
     * 构造应用服务。
     *
     * @param restaurantMapper 餐馆数据访问组件
     */
    public RestaurantAppService(RestaurantMapper restaurantMapper) {
        this.restaurantMapper = restaurantMapper;
    }

    /**
     * 创建餐馆。
     *
     * @param request 创建请求
     * @return 创建后的餐馆信息
     */
    @Transactional
    public RestaurantDTO create(RestaurantCreateRequest request) {
        Instant now = Instant.now();
        RestaurantEntity entity = new RestaurantEntity();
        entity.setName(request.name());
        entity.setCampus(request.campus());
        entity.setAddress(request.address());
        entity.setDescription(request.description());
        entity.setCoverImageUrl(request.coverImageUrl());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        restaurantMapper.insert(entity);
        return toDTO(entity);
    }

    /**
     * 按 ID 查询餐馆。
     *
     * @param id 餐馆 ID
     * @return 餐馆信息
     * @throws BizException 餐馆不存在时抛出
     */
    public RestaurantDTO getById(Long id) {
        RestaurantEntity entity = restaurantMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "餐馆不存在");
        }
        return toDTO(entity);
    }

    /**
     * 餐馆检索。
     *
     * @param name 餐馆名称（模糊匹配，可选）
     * @param campus 校区（精确匹配，可选）
     * @return 结果列表（按 ID 倒序）
     */
    public List<RestaurantDTO> search(String name, String campus) {
        LambdaQueryWrapper<RestaurantEntity> qw = new LambdaQueryWrapper<>();
        if (name != null && !name.isBlank()) {
            qw.like(RestaurantEntity::getName, name.trim());
        }
        if (campus != null && !campus.isBlank()) {
            qw.eq(RestaurantEntity::getCampus, campus.trim());
        }
        qw.orderByDesc(RestaurantEntity::getId);
        return restaurantMapper.selectList(qw).stream().map(this::toDTO).toList();
    }

    /**
     * 将实体映射为对外 DTO。
     *
     * @param entity 餐馆实体
     * @return 餐馆 DTO
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
