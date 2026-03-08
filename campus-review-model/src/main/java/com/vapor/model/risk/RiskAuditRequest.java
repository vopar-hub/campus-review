package com.vapor.model.risk;

import jakarta.validation.constraints.NotBlank;

/**
 * 内容审核请求体。
 *
 * @param content 待审核文本
 */
public record RiskAuditRequest(@NotBlank String content) {
}
