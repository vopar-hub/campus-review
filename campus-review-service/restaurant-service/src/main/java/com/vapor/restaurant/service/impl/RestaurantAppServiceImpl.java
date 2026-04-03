package com.vapor.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.vapor.common.error.BizException;
import com.vapor.common.error.ErrorCode;
import com.vapor.model.restaurant.RestaurantCreateRequest;
import com.vapor.model.restaurant.RestaurantDTO;
import com.vapor.restaurant.entity.RestaurantEntity;
import com.vapor.restaurant.mapper.RestaurantMapper;
import com.vapor.restaurant.service.MinioService;
import com.vapor.restaurant.service.RestaurantAppService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

/**
 * 餐馆应用服务实现类。
 */
@Service
public class RestaurantAppServiceImpl implements RestaurantAppService {
    private final RestaurantMapper restaurantMapper;
    private final MinioService minioService;

    /**
     * 构造应用服务。
     *
     * @param restaurantMapper 餐馆数据访问组件
     * @param minioService MinIO 文件服务
     */
    public RestaurantAppServiceImpl(RestaurantMapper restaurantMapper, MinioService minioService) {
        this.restaurantMapper = restaurantMapper;
        this.minioService = minioService;
    }

    @Override
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

    @Override
    @Transactional
    public RestaurantDTO createWithImage(RestaurantCreateRequest request, MultipartFile coverImage) {
        // 上传图片，返回对象路径
        String objectPath = minioService.uploadFile(coverImage, "restaurants");

        // 创建餐厅，存储对象路径
        RestaurantCreateRequest newRequest = new RestaurantCreateRequest(
                request.name(),
                request.campus(),
                request.address(),
                request.description(),
                objectPath
        );
        return create(newRequest);
    }

    @Override
    public RestaurantDTO getById(Long id) {
        RestaurantEntity entity = restaurantMapper.selectById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "餐馆不存在");
        }
        return toDTO(entity);
    }

    @Override
    public List<RestaurantDTO> getByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<RestaurantEntity> entities = restaurantMapper.selectBatchIds(ids);
        return entities.stream().map(this::toDTO).toList();
    }

    @Override
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
        String imageUrl = entity.getCoverImageUrl();

        // 动态生成访问 URL
        if (imageUrl != null && !imageUrl.isBlank()) {
            imageUrl = minioService.getFileUrl(imageUrl);
        }

        return new RestaurantDTO(
                entity.getId(),
                entity.getName(),
                entity.getCampus(),
                entity.getAddress(),
                entity.getDescription(),
                imageUrl,
                entity.getCreatedAt()
        );
    }
}
