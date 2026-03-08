package com.vapor.common.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BizException 单元测试。
 */
class BizExceptionTest {

    @Test
    void testConstructorWithErrorCode() {
        BizException exception = new BizException(ErrorCode.BAD_REQUEST);
        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        assertEquals("请求参数错误", exception.getMessage());
    }

    @Test
    void testConstructorWithMessage() {
        BizException exception = new BizException(ErrorCode.BAD_REQUEST, "Custom message");
        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        assertEquals("Custom message", exception.getMessage());
    }

    @Test
    void testGetErrorCode() {
        BizException exception = new BizException(ErrorCode.UNAUTHORIZED);
        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
    }

    @Test
    void testInheritFromRuntimeException() {
        BizException exception = new BizException(ErrorCode.INTERNAL_ERROR);
        assertTrue(exception instanceof RuntimeException);
    }
}
