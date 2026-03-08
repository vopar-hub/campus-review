package com.vapor.model.interaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 互动请求体。
 *
 * 用于点赞/取消点赞、收藏/取消收藏等互动操作的目标描述。
 *
 * @param targetType 目标类型（如 RESTAURANT、REVIEW）
 * @param targetId 目标 ID
 */
public record InteractRequest(
        @NotBlank String targetType,
        @NotNull Long targetId
) {
}
