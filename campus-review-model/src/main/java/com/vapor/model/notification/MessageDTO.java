package com.vapor.model.notification;

import java.time.Instant;

/**
 * 站内消息 DTO。
 *
 * @param id 消息 ID
 * @param toUserId 接收用户 ID
 * @param title 标题
 * @param content 内容
 * @param read 是否已读
 * @param createdAt 创建时间
 */
public record MessageDTO(
        Long id,
        Long toUserId,
        String title,
        String content,
        boolean read,
        Instant createdAt
) {
}
