package com.vapor.restaurant.service;

import com.vapor.model.restaurant.RestaurantCreateRequest;
import com.vapor.model.restaurant.RestaurantDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 餐馆应用服务接口。
 *
 * 负责餐馆创建、查询与检索等核心业务。
 */
public interface RestaurantAppService {

    /**
     * 创建餐馆。
     *
     * @param request 创建请求
     * @return 创建后的餐馆信息
     */
    RestaurantDTO create(RestaurantCreateRequest request);

    /**
     * 创建餐馆（带图片上传）。
     *
     * @param request 创建请求
     * @param coverImage 封面图片文件
     * @return 创建后的餐馆信息
     */
    RestaurantDTO createWithImage(RestaurantCreateRequest request, MultipartFile coverImage);

    /**
     * 按 ID 查询餐馆。
     *
     * @param id 餐馆 ID
     * @return 餐馆信息
     */
    RestaurantDTO getById(Long id);

    /**
     * 根据 ID 列表批量查询餐馆。
     *
     * @param ids 餐馆 ID 列表
     * @return 餐馆列表
     */
    List<RestaurantDTO> getByIds(List<Long> ids);

    /**
     * 餐馆检索。
     *
     * @param name 餐馆名称（模糊匹配，可选）
     * @param campus 校区（精确匹配，可选）
     * @return 结果列表（按 ID 倒序）
     */
    List<RestaurantDTO> search(String name, String campus);
}
