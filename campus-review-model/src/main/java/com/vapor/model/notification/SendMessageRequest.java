package com.vapor.model.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 发送站内消息请求体。
 *
 * @param toUserId 接收用户 ID
 * @param title 标题
 * @param content 内容
 */
public record SendMessageRequest(
        @NotNull Long toUserId,
        @NotBlank String title,
        @NotBlank String content
) {
}
