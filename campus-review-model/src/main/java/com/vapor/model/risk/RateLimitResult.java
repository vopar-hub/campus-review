package com.vapor.model.risk;

/**
 * 限流判定结果。
 *
 * @param allowed 是否允许通过
 * @param remaining 当前窗口剩余次数
 * @param resetAtEpochMillis 窗口重置时间（毫秒时间戳）
 */
public record RateLimitResult(
        boolean allowed,
        long remaining,
        long resetAtEpochMillis
) {
}
