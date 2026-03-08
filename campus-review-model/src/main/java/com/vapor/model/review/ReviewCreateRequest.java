package com.vapor.model.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 发布评价请求体。
 *
 * @param restaurantId 餐馆 ID
 * @param rating 评分（1~5）
 * @param content 评价内容
 */
public record ReviewCreateRequest(
        @NotNull Long restaurantId,
        @Min(1) @Max(5) int rating,
        @NotBlank String content
) {
}
