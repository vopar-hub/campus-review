package com.vapor.model.restaurant;

import jakarta.validation.constraints.NotBlank;

/**
 * 新增餐馆请求体。
 *
 * @param name 餐馆名称
 * @param campus 校区
 * @param address 地址
 * @param description 描述
 * @param coverImageUrl 封面图 URL
 */
public record RestaurantCreateRequest(
        @NotBlank String name,
        @NotBlank String campus,
        String address,
        String description,
        String coverImageUrl
) {
}
