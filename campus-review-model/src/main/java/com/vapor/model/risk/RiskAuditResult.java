package com.vapor.model.risk;

/**
 * 内容审核结果。
 *
 * @param allowed 是否允许通过
 * @param reason 拦截原因（允许时可为空）
 */
public record RiskAuditResult(
        boolean allowed,
        String reason
) {
}
