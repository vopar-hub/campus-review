package com.vapor.riskcontrol.service;

import com.vapor.model.risk.RateLimitResult;
import com.vapor.model.risk.RiskAuditResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 风控应用服务。
 *
 * 负责内容审核（敏感词）与限流判定等基础风控能力。
 */
@Service
public class RiskControlAppService {
    private static final Logger log = LoggerFactory.getLogger(RiskControlAppService.class);

    private final Set<String> keywords;
    private final FixedWindowRateLimiter localRateLimiter;
    private final RedisSlidingWindowRateLimiter redisRateLimiter;
    private final boolean redisEnabled;

    /**
     * 构造应用服务。
     *
     * @param keywordsCsv 敏感词列表（逗号分隔）
     * @param localRateLimiter 本地限流器（备用）
     * @param redisRateLimiter Redis 限流器（主）
     * @param redisEnabled Redis 是否可用（从配置文件读取）
     */
    public RiskControlAppService(
            @Value("${risk.keywords:}") String keywordsCsv,
            FixedWindowRateLimiter localRateLimiter,
            RedisSlidingWindowRateLimiter redisRateLimiter,
            @Value("${spring.data.redis.host:localhost}") String redisHost) {
        this.keywords = parseKeywords(keywordsCsv);
        this.localRateLimiter = localRateLimiter;
        this.redisRateLimiter = redisRateLimiter;
        this.redisEnabled = !"localhost".equals(redisHost) || System.getenv("REDIS_HOST") != null;
        log.info("风控服务初始化完成，加载 {} 个敏感词，Redis 限流：{}", keywords.size(), redisEnabled ? "已启用" : "未启用（使用本地限流）");
    }

    /**
     * 审核文本内容是否可发布。
     *
     * @param content 文本内容
     * @return 审核结果（通过/拦截及原因）
     */
    public RiskAuditResult auditContent(String content) {
        if (content == null) {
            return new RiskAuditResult(true, null);
        }

        String lower = content.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (!keyword.isBlank() && lower.contains(keyword)) {
                log.info("内容审核拦截：包含敏感词，contentLength={}", content.length());
                return new RiskAuditResult(false, "包含敏感词");
            }
        }
        return new RiskAuditResult(true, null);
    }

    /**
     * 滑动窗口限流判定（优先使用 Redis，不可用时降级到本地）。
     *
     * @param key 限流键（可为用户 ID、IP 等）
     * @param limit 窗口内最大允许次数
     * @param windowSeconds 窗口大小（秒）
     * @return 限流结果（是否允许、剩余次数、重置时间）
     */
    public RateLimitResult rateLimit(String key, long limit, long windowSeconds) {
        RedisSlidingWindowRateLimiter.RateLimitDecision decision;
        if (redisEnabled) {
            decision = redisRateLimiter.allow(key, limit, windowSeconds);
        } else {
            FixedWindowRateLimiter.RateLimitDecision localDecision = localRateLimiter.allow(key, limit, windowSeconds);
            decision = new RedisSlidingWindowRateLimiter.RateLimitDecision(
                    localDecision.allowed(),
                    localDecision.remaining(),
                    localDecision.resetAtEpochMillis()
            );
        }

        if (!decision.allowed()) {
            log.warn("限流拦截：key={}, limit={}, windowSeconds={}, remaining={}",
                    key, limit, windowSeconds, decision.remaining());
        }

        return new RateLimitResult(decision.allowed(), decision.remaining(), decision.resetAtEpochMillis());
    }

    /**
     * 解析敏感词配置。
     *
     * @param csv 逗号分隔的敏感词（支持中英文逗号）
     * @return 规范化（小写、去空白）后的敏感词集合
     */
    private static Set<String> parseKeywords(String csv) {
        if (csv == null || csv.isBlank()) {
            return Set.of();
        }
        // 支持中文逗号和英文逗号分隔
        return Arrays.stream(csv.split(",|，"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }
}
