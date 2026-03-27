package com.vapor.ranking.client.fallback;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.interaction.InteractionCountDTO;
import com.vapor.ranking.client.InteractionServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 互动服务 Feign 客户端降级处理。
 */
@Component
public class InteractionServiceClientFallback implements InteractionServiceClient {
    private static final Logger log = LoggerFactory.getLogger(InteractionServiceClientFallback.class);

    @Override
    public ApiResponse<InteractionCountDTO> getCount(String targetType, Long targetId) {
        log.error("获取互动计数失败，targetType={}, targetId={}", targetType, targetId);
        // 返回默认的 0 值
        return ApiResponse.ok(new InteractionCountDTO(targetType, targetId, 0, 0));
    }
}
