package com.vapor.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.vapor.common.error.BizException;
import com.vapor.common.error.ErrorCode;
import com.vapor.common.util.UserContextUtil;
import com.vapor.model.notification.MessageDTO;
import com.vapor.model.notification.SendMessageRequest;
import com.vapor.notification.entity.MessageEntity;
import com.vapor.notification.mapper.MessageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * 通知应用服务。
 *
 * 提供站内消息投递、收件箱查询与已读标记等核心能力。
 */
@Service
public class NotificationAppService {
    private static final Logger log = LoggerFactory.getLogger(NotificationAppService.class);

    private final MessageMapper messageMapper;

    /**
     * 构造应用服务。
     *
     * @param messageMapper 消息数据访问组件
     */
    public NotificationAppService(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    /**
     * 投递站内消息。
     *
     * @param request 投递请求
     * @return 消息信息
     */
    @Transactional
    public MessageDTO send(SendMessageRequest request) {
        log.info("发送消息：toUserId={}, title={}", request.toUserId(), request.title());

        MessageEntity entity = new MessageEntity();
        entity.setToUserId(request.toUserId());
        entity.setTitle(request.title());
        entity.setContent(request.content());
        entity.setReadFlag(false);
        entity.setCreatedAt(Instant.now());
        messageMapper.insert(entity);

        log.info("消息发送成功：messageId={}", entity.getId());
        return toDTO(entity);
    }

    /**
     * 查询当前用户收件箱。
     *
     * @return 收件箱消息列表（按 ID 倒序）
     * @throws BizException 未登录时抛出
     */
    public List<MessageDTO> inbox() {
        Long userId = UserContextUtil.requireUserId();
        log.debug("查询收件箱：userId={}", userId);

        return messageMapper.selectList(new LambdaQueryWrapper<MessageEntity>()
                        .eq(MessageEntity::getToUserId, userId)
                        .orderByDesc(MessageEntity::getId))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * 标记消息为已读。
     *
     * @param id 消息 ID
     * @throws BizException 未登录或消息不存在时抛出
     */
    @Transactional
    public void markRead(Long id) {
        Long userId = UserContextUtil.requireUserId();
        log.info("标记消息已读：messageId={}, userId={}", id, userId);

        int updated = messageMapper.update(new LambdaUpdateWrapper<MessageEntity>()
                .eq(MessageEntity::getId, id)
                .eq(MessageEntity::getToUserId, userId)
                .set(MessageEntity::getReadFlag, true));
        if (updated == 0) {
            throw new BizException(ErrorCode.NOT_FOUND, "消息不存在");
        }
    }

    /**
     * 将实体映射为对外 DTO。
     *
     * @param entity 消息实体
     * @return 消息 DTO
     */
    private MessageDTO toDTO(MessageEntity entity) {
        return new MessageDTO(
                entity.getId(),
                entity.getToUserId(),
                entity.getTitle(),
                entity.getContent(),
                Boolean.TRUE.equals(entity.getReadFlag()),
                entity.getCreatedAt()
        );
    }
}
