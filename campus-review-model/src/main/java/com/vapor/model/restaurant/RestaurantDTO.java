package com.vapor.model.restaurant;

import java.time.Instant;

/**
 * 餐馆 DTO。
 *
 * @param id 餐馆 ID
 * @param name 名称
 * @param campus 校区
 * @param address 地址
 * @param description 描述
 * @param coverImageUrl 封面图 URL
 * @param createdAt 创建时间
 */
public record RestaurantDTO(
        Long id,
        String name,
        String campus,
        String address,
        String description,
        String coverImageUrl,
        Instant createdAt
) {
}
