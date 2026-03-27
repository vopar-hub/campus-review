package com.vapor.ranking.client;

import com.vapor.common.api.ApiResponse;
import com.vapor.model.interaction.InteractionCountDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 互动服务 Feign 客户端。
 */
@FeignClient(name = "interaction-service", fallback = InteractionServiceClientFallback.class)
public interface InteractionServiceClient {

    /**
     * 获取指定目标的互动计数。
     *
     * @param targetType 目标类型
     * @param targetId 目标 ID
     * @return 互动计数响应
     */
    @GetMapping("/api/interactions/count")
    ApiResponse<InteractionCountDTO> getCount(
            @RequestParam("targetType") String targetType,
            @RequestParam("targetId") Long targetId
    );
}
