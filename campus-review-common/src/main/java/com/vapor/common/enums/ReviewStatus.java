package com.vapor.common.enums;

import lombok.Getter;

/**
 * 评价状态枚举。
 */
@Getter
public enum ReviewStatus {
    /**
     * 待审核
     */
    PENDING("PENDING"),

    /**
     * 已通过
     */
    APPROVED("APPROVED"),

    /**
     * 已驳回
     */
    REJECTED("REJECTED");

    private final String code;

    ReviewStatus(String code) {
        this.code = code;
    }

    /**
     * 根据代码获取枚举。
     *
     * @param code 状态代码
     * @return 对应的枚举值，找不到返回 null
     */
    public static ReviewStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ReviewStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
