package com.vapor.common.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ErrorCode 单元测试。
 */
class ErrorCodeTest {

    @Test
    void testErrorCode_Values() {
        assertEquals(0, ErrorCode.OK.getCode());
        assertEquals("OK", ErrorCode.OK.getMessage());

        assertEquals(40000, ErrorCode.BAD_REQUEST.getCode());
        assertEquals("请求参数错误", ErrorCode.BAD_REQUEST.getMessage());

        assertEquals(40100, ErrorCode.UNAUTHORIZED.getCode());
        assertEquals("未登录或登录已过期", ErrorCode.UNAUTHORIZED.getMessage());

        assertEquals(40300, ErrorCode.FORBIDDEN.getCode());
        assertEquals("无权限", ErrorCode.FORBIDDEN.getMessage());

        assertEquals(40400, ErrorCode.NOT_FOUND.getCode());
        assertEquals("资源不存在", ErrorCode.NOT_FOUND.getMessage());

        assertEquals(42900, ErrorCode.TOO_MANY_REQUESTS.getCode());
        assertEquals("请求过于频繁", ErrorCode.TOO_MANY_REQUESTS.getMessage());

        assertEquals(50000, ErrorCode.INTERNAL_ERROR.getCode());
        assertEquals("服务器内部错误", ErrorCode.INTERNAL_ERROR.getMessage());
    }
}
