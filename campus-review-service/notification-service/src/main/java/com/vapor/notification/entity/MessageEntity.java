package com.vapor.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;

/**
 * 站内消息表实体。
 *
 * 对应数据库 messages 表，记录接收人、标题/内容、已读状态与创建时间。
 */
@Data
@TableName("messages")
public class MessageEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long toUserId;
    private String title;
    private String content;
    private Boolean readFlag;
    private Instant createdAt;
}
