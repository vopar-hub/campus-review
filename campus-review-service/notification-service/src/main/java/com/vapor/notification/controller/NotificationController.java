package com.vapor.notification.controller;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.notification.MessageDTO;
import com.vapor.model.notification.SendMessageRequest;
import com.vapor.notification.service.NotificationAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 通知接口（用户侧）。
 *
 * 提供站内信投递、收件箱查询与已读标记等能力。
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "通知管理", description = "站内信投递、收件箱查询等接口")
public class NotificationController {
    private final NotificationAppService notificationAppService;

    /**
     * 构造控制器。
     *
     * @param notificationAppService 通知应用服务
     */
    public NotificationController(NotificationAppService notificationAppService) {
        this.notificationAppService = notificationAppService;
    }

    /**
     * 投递站内消息。
     *
     * @param request 投递请求
     * @return 消息信息
     */
    @PostMapping("/send")
    @Operation(summary = "投递消息", description = "投递站内消息给指定用户")
    public ApiResponse<MessageDTO> send(@Valid @RequestBody SendMessageRequest request) {
        return ApiResponse.ok(notificationAppService.send(request));
    }

    /**
     * 查询当前用户收件箱。
     *
     * @return 收件箱消息列表
     */
    @GetMapping("/inbox")
    @Operation(summary = "查询收件箱", description = "查询当前用户的收件箱消息列表")
    public ApiResponse<List<MessageDTO>> inbox() {
        return ApiResponse.ok(notificationAppService.inbox());
    }

    /**
     * 标记指定消息为已读。
     *
     * @param id 消息 ID
     * @return 空响应体
     */
    @PostMapping("/{id}/read")
    @Operation(summary = "标记消息已读", description = "将指定消息标记为已读")
    public ApiResponse<Void> read(@PathVariable Long id) {
        notificationAppService.markRead(id);
        return ApiResponse.ok(null);
    }
}
