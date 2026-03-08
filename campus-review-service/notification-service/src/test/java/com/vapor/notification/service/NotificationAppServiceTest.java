package com.vapor.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.vapor.common.error.BizException;
import com.vapor.model.notification.MessageDTO;
import com.vapor.model.notification.SendMessageRequest;
import com.vapor.notification.entity.MessageEntity;
import com.vapor.notification.mapper.MessageMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * NotificationAppService 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class NotificationAppServiceTest {

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private NotificationAppService notificationService;

    private SendMessageRequest sendRequest;
    private MessageEntity testEntity;

    @BeforeEach
    void setUp() {
        sendRequest = new SendMessageRequest(1L, "系统通知", "欢迎使用校园美食点评平台！");

        testEntity = new MessageEntity();
        testEntity.setId(1L);
        testEntity.setToUserId(1L);
        testEntity.setTitle("系统通知");
        testEntity.setContent("欢迎使用校园美食点评平台！");
        testEntity.setReadFlag(false);
        testEntity.setCreatedAt(java.time.Instant.now());
    }

    @Test
    @DisplayName("发送消息成功")
    void send_success() {
        // Given
        when(messageMapper.insert(any(MessageEntity.class))).thenAnswer(invocation -> {
            MessageEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return 1;
        });

        // When
        MessageDTO result = notificationService.send(sendRequest);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("系统通知", result.title());
        assertEquals(1L, result.toUserId());
        assertFalse(result.read());
        verify(messageMapper).insert(any(MessageEntity.class));
    }

    @Test
    @DisplayName("发送消息 - 验证内容")
    void send_verifyContent() {
        // Given
        SendMessageRequest request = new SendMessageRequest(2L, "测试标题", "测试内容");
        when(messageMapper.insert(any(MessageEntity.class))).thenAnswer(invocation -> {
            MessageEntity entity = invocation.getArgument(0);
            entity.setId(2L);
            return 1;
        });

        // When
        MessageDTO result = notificationService.send(request);

        // Then
        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals("测试标题", result.title());
        assertEquals("测试内容", result.content());
    }
}
